package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitorSimpleImpl
import com.kazurayam.materials.repository.RepositoryWalker

import groovy.json.JsonOutput
import groovy.xml.XmlUtil


class IndexerByVisitorImpl implements Indexer {

    static Logger logger_ = LoggerFactory.getLogger(IndexerByVisitorImpl.class)

    private Path baseDir_
    private Path reportsDir_
    private Path output_

    IndexerByVisitorImpl() {
        baseDir_ = null
        reportsDir_ = null
        output_ = null
    }

    @Override
    Path getOutput() {
        return this.output_
    }
    
    @Override
    void setBaseDir(Path baseDir) {
        if (baseDir == null) {
            def msg = "#setBaseDir baseDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(baseDir)) {
            def msg = "#setBaseDir basedir ${baseDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        baseDir_ = baseDir
    }

    @Override
    void setReportsDir(Path reportsDir) {
        if (reportsDir == null) {
            def msg = "#setReportsDir reportsDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(reportsDir)) {
            def msg = "#setReportsDir reportsDir ${reportsDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        reportsDir_ = reportsDir
    }
    
    @Override
    void setOutput(Path outputFile) {
        Objects.requireNonNull(outputFile)
        output_ = outputFile
        Helpers.ensureDirs(outputFile.getParent())
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    void execute() throws IOException {
        if (baseDir_ == null) {
            def msg = "#execute baseDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (reportsDir_ == null) {
            def msg = "#execute reportsDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (output_ == null) {
            def msg = "#execute output_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_, reportsDir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        OutputStream os = output_.toFile().newOutputStream()
        generate(repoRoot, os)
        logger_.info("generated ${output_.toString()}")
    }

    void generate(RepositoryRoot repoRoot, OutputStream os) throws IOException {
        Objects.requireNonNull(repoRoot)
        Objects.requireNonNull(os)
        def dir = repoRoot.getBaseDir().resolve('../..').normalize().toAbsolutePath()
        def title = dir.relativize(repoRoot.getBaseDir().normalize().toAbsolutePath()).toString()
        def writer = new OutputStreamWriter(os, 'UTF-8')
        //
        StringWriter htmlFragments = new StringWriter()
        def htmlVisitor = new RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlFragments)
        RepositoryWalker.walkRepository(repoRoot, htmlVisitor)
        //
        StringWriter jsonSnippet = new StringWriter()
        def jsonVisitor = new RepositoryVisitorGeneratingBootstrapTreeviewData(jsonSnippet)
        RepositoryWalker.walkRepository(repoRoot, jsonVisitor)
        //
        StringBuilder sb = new StringBuilder()
        sb.append('<html>'                                                                    + "\n")
        sb.append('  <head>'                                                                  + "\n")
        sb.append('    <meta http-equiv="X-UA-Compatible" content="IE=edge" />'               + "\n")
        sb.append('    <title>' + title + '</title>' + "\n")
        sb.append('    <meta charset="utf-8" />'                                              + "\n")
        sb.append('    <meta name="description" content="" />'                                + "\n")
        sb.append('    <meta name="author" content="" />'                                     + "\n")
        sb.append('    <meta name="viewport" content="width=device-width, initial-scale=1" />'+ "\n")
        sb.append('    <link rel="stylesheet" href="" />'                                     + "\n")
        sb.append('<!-- [if lt IE 9]'                                                         + "\n")
        sb.append('<script src="//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js"></script>' + "\n")
        sb.append('<script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js"></script>' + "\n")
        sb.append('<![endif] -->'                                                             + "\n")
        sb.append('    <link rel="shortcut icon" href="" />'                                  + "\n")
        sb.append('    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css"')
        sb.append(' rel="stylesheet"')
        sb.append(' integrity="sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB"')
        sb.append(' crossorigin="anonymous" />'                                               + "\n")
        sb.append('    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.css"')
        sb.append(' rel="stylesheet" />'                                                      + "\n")
        sb.append('    <style>'                                                               + "\n")
        sb.append('.list-group-item > .badge {'                                               + "\n")
        sb.append('    float: right;'                                                         + "\n")
        sb.append('}'                                                                         + "\n")
        sb.append('    </style>'                                                              + "\n")
        sb.append('  </head>'                                                                 + "\n")
        sb.append('  <body>'                                                                  + "\n")
        sb.append('    <div class="container">'                                               + "\n")
        sb.append('      <h3>' + title + '</h3>'                                              + "\n")
        sb.append('      <div id="tree"></div>'                                               + "\n")
        sb.append('      <div id="footer"></div>'                                             + "\n")
        sb.append('      <div id="modal-windows">'                                            + "\n")
        sb.append('<!-- here reporoot.htmlFragmentsOfMaterialsAsModal() is inserted -->'  + "\n\n\n")
        sb.append(         htmlFragments.toString()                                           + "\n\n\n")
        //
        sb.append('<!-- end of reporoot.htmlFragmentsOfMaterialsAsModal() -->'                + "\n")
        sb.append('      </div>'                                                              + "\n")
        sb.append('    </div>'                                                                + "\n")
        sb.append('    <!-- SCRIPTS -->'                                                      + "\n")
        sb.append('    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"'              )
        sb.append(' integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"')
        sb.append(' crossorigin="anonymous"></script>'                                        + "\n")
        sb.append('    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"')
        sb.append(' integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"')
        sb.append(' crossorigin="anonymous"></script>'                                        + "\n")
        sb.append('    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js"')
        sb.append(' integrity="sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T"')
        sb.append(' crossorigin="anonymous"></script>'                                        + "\n")
        sb.append('    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.js"')
        sb.append('></script>'                                                                + "\n")
        sb.append('    <script type="text/javascript"><!--'                                   + "\n")
        sb.append('''
function getTree() {
    var data = ''' + JsonOutput.prettyPrint(jsonSnippet.toString()) + ''';
    return data;
}
//
function modalize() {
    $('#tree a').each(function() {
        if ($(this).attr('href') && $(this).attr('href') != '#') {
            $(this).attr('data-toggle', 'modal');
            $(this).attr('data-target', $(this).attr('href'));
            $(this).attr('href', '#');
        }
    });
}
//
$('#tree').treeview({
    data: getTree(),
    enableLinks: true,
    levels: 1,
    multiSelect: false,
    showTags: true,
    onNodeSelected: function(event, data) {
        modalize();
    }
});
//
modalize();
''')
        sb.append('--></script>'                                                              + "\n")
        sb.append('  </body>'                                                                 + "\n")
        sb.append('</html>'                                                                   + "\n")

        //
        String html = sb.toString()
        writer.write(html)
        writer.flush()
        writer.close()

    }

    /**
     *
     * @author kazurayam
     *
     */
    static class RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal
            extends RepositoryVisitorSimpleImpl implements RepositoryVisitor {
        static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal.class)
        RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(Writer writer) {
            super(writer)
        }
        @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {}
        @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {}
        @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
        @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
        @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {}
        @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {}

        @Override RepositoryVisitResult visitMaterial(Material material) {
            StringBuilder sb = new StringBuilder()
            sb.append('<div id="' + material.hashCode() + '" class="modal fade">' + "\n")
            sb.append('  <div class="modal-dialog modal-lg" role="document">' + "\n")
            sb.append('    <div class="modal-content">' + "\n")
            sb.append('      <div class="modal-header">' + "\n")
            sb.append('        <p class="modal-title" id="')
            sb.append(material.hashCode() + 'title')
            sb.append('">')
            sb.append(material.getIdentifier())
            sb.append('</p>' + "\n")
            sb.append('      </div>' + "\n")
            sb.append('      <div class="modal-body">' + "\n")
            sb.append('        ' + markupInModalWindow(material) + "\n")
            sb.append('      </div>' + "\n")
            sb.append('      <div class="modal-footer">' + "\n")
            sb.append('        <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>' + "\n")
            def ar = anchorToReport(material)
            if (ar != null) {
                sb.append('        ' + ar + "\n")
            }
            sb.append('      </div>' + "\n")
            sb.append('    </div>' + "\n")
            sb.append('  </div>' + "\n")
            sb.append('</div>' + "\n")
            pw_.print(sb.toString())
            pw_.flush()
            return RepositoryVisitResult.SUCCESS
        }

        @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
            throw new UnsupportedOperationException("failed visiting " + material.toString())
        }

        /**
         *
         * @return
         */
        String markupInModalWindow(Material mate) {
            StringBuilder sb = new StringBuilder()
            switch (mate.getFileType()) {
                case FileType.BMP:
                case FileType.GIF:
                case FileType.JPG:
                case FileType.JPEG:
                case FileType.PNG:
                    sb.append('<img src="' + mate.getEncodedHrefRelativeToRepositoryRoot() +
                        '" class="img-fluid" style="border: 1px solid #ddd" alt="material"></img>')
                    break
                case FileType.CSV:
                    def content = mate.getPath().toFile().getText('UTF-8')
                    sb.append('<pre class="pre-scrollable"><code>')
                    sb.append(escapeHtml(content))
                    sb.append('</code></pre>')
                    break
                case FileType.TXT:
                    sb.append('<div style="height:350px;overflow:auto;">')
                    def content = mate.getPath().toFile().getText('UTF-8')
                    def file = mate.getPath().toFile()
                    file.readLines('UTF-8').each { line ->
                        sb.append('<p>')
                        sb.append(escapeHtml(line))
                        sb.append('</p>' + "\n")
                    }
                    sb.append('</div>' + "\n")
                    break
                case FileType.JSON:
                    def content = mate.getPath().toFile().getText('UTF-8')
                    content = JsonOutput.prettyPrint(content)
                    sb.append('<pre class="pre-scrollable"><code>')
                    sb.append(escapeHtml(content))
                    sb.append('</code></pre>')
                    break
                case FileType.XML:
                    def content = mate.getPath().toFile().getText('UTF-8')
                    content = XmlUtil.serialize(content)
                    sb.append('<pre class="pre-scrollable"><code>')
                    sb.append(escapeHtml(content))
                    sb.append('</code></pre>')
                    break
                case FileType.PDF:
                    sb.append('<div class="embed-responsive embed-responsive-16by9" style="padding-bottom:150%">' + "\n")
                    sb.append('  <object class="embed-responsive-item" data="')
                    sb.append(mate.getEncodedHrefRelativeToRepositoryRoot())
                    sb.append('" type="application/pdf" width="100%" height="100%"></object>' + "\n")
                    sb.append('</div>' + "\n")
                    sb.append('<div><a href="' + mate.getEncodedHrefRelativeToRepositoryRoot() + '">')
                    sb.append(mate.getPathRelativeToRepositoryRoot())
                    sb.append('</a></div>')
                    break
                case FileType.XLS:
                case FileType.XLSM:
                case FileType.XLSX:
                    sb.append('<a class="btn btn-primary btn-lg" target="_blank" href="')
                    sb.append(mate.getEncodedHrefRelativeToRepositoryRoot())
                    sb.append('">')
                    sb.append('Download')
                    sb.append('</a>')
                    break
                default:
                    def msg = "this.getFileType()='${mate.getFileType()}' is unexpected"
                    logger_.warn('#markupInModalWindow ' + msg)
                    sb.append("        <p>${msg}</p>")
            }
            return sb.toString()
        }

        /**
         * Escape HTML angle brackets in the given string. For example.,
         * & --- &amp;
         * < --- &lt;
         * > --- &gt;
         * " --- &quot;
         *   --- &nbsp;
         * © --- &copy;
         *
         * @param str
         * @return
         */
        static String escapeHtml(String str) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = str.toCharArray();
            for (char ch : charArray) {
                switch (ch) {
                    case '&': sb.append('&amp;'); break;
                    case '<': sb.append('&lt;'); break;
                    case '>': sb.append('&gt;'); break;
                    case '"': sb.append('&quot;'); break;
                    case ' ': sb.append('&nbsp;'); break;
                    case '©': sb.append('&copy;'); break;
                    default : sb.append(ch); break;
                }
            }
            return sb.toString();
        }

        String anchorToReport(Material mate) {
            String reportHref = hrefToReport(mate)
            if (reportHref != null) {
                Path p = mate.getParent().getParent().getRepositoryRoot().getBaseDir().resolve(reportHref)
                if (Files.exists(p)) {
                    StringBuilder sb = new StringBuilder()
                    sb.append('<a href="')
                    sb.append(reportHref)
                    sb.append('" class="btn btn-default" role="button" target="_blank">Report</a>')
                    return sb.toString()
                } else {
                    logger_.debug("#anchorToReport ${p} does not exist")
                    return null
                }
            } else {
                logger_.debug("#anchorToReport this.hrefToReport() returned null")
                return null
            }
        }

        String hrefToReport(Material mate) {
            TSuiteResult tsr = mate.getParent().getParent()
            if (tsr != null) {
                StringBuilder sb = new StringBuilder()
                sb.append('../Reports/')
                sb.append(tsr.getId().getTSuiteName().getValue().replace('.', '/'))
                sb.append('/')
                sb.append(tsr.getId().getTSuiteTimestamp().format())
                sb.append('/Report.html')
                return sb.toString()
            } else {
                return null
            }
        }
    }


    /**
     *
     * @author kazurayam
     *
     */
    static class RepositoryVisitorGeneratingBootstrapTreeviewData
            extends RepositoryVisitorSimpleImpl implements RepositoryVisitor {
         static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingBootstrapTreeviewData.class)
         private int tSuiteResultCount
         private int tCaseResultCount
         private int materialsCount
         RepositoryVisitorGeneratingBootstrapTreeviewData(Writer writer) {
             super(writer)
         }
         @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
             tSuiteResultCount = 0
             pw_.print('[')
             pw_.flush()
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
             pw_.print(']')
             pw_.flush()
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {
             if (tSuiteResultCount > 0) {
                 pw_.print(',')
             }
             tSuiteResultCount += 1
             //
             StringBuilder sb = new StringBuilder()
             sb.append('{')
             sb.append('"text":"' + Helpers.escapeAsJsonText(tSuiteResult.treeviewTitle()) + '",')
             sb.append('"backColor":"#CCDDFF",')
             sb.append('"selectable":false,')
             sb.append('"state":{')
             sb.append('    "expanded":' + tSuiteResult.isLatestModified() )
             sb.append('},')
             sb.append('"nodes":[')
             pw_.print(sb.toString())
             pw_.flush()
             tCaseResultCount = 0
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {
             StringBuilder sb = new StringBuilder()
             sb.append(']')
             if (tSuiteResult.getJUnitReportWrapper() != null) {
                 sb.append(',')
                 sb.append('"tags": ["')
                 logger_.debug("#toBootstrapTreeviewData this.getTSuiteName() is '${tSuiteResult.getId().getTSuiteName()}'")
                 sb.append(tSuiteResult.getJUnitReportWrapper().getTestSuiteSummary(tSuiteResult.getId().getTSuiteName().getId()))
                 sb.append('"')
                 sb.append(',')
                 sb.append('"')
                 sb.append("${tSuiteResult.getExecutionPropertiesWrapper().getExecutionProfile()}")
                 sb.append('"')
                 sb.append(',')
                 sb.append('"')
                 sb.append("${tSuiteResult.getExecutionPropertiesWrapper().getDriverName()}")
                 sb.append('"')
                 sb.append(']')
             }
             sb.append('}')
             pw_.print(sb.toString())
             pw_.flush()
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {
             StringBuilder sb = new StringBuilder()
             if (tCaseResultCount > 0) {
                 sb.append(',')
             }
             tCaseResultCount += 1
             //
             sb.append('{')
             sb.append('"text":"' + Helpers.escapeAsJsonText(tCaseResult.getTCaseName().getValue())+ '",')
             sb.append('"selectable":false,')
             sb.append('"nodes":[')
             pw_.print(sb.toString())
             pw_.flush()
             materialsCount = 0
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
             StringBuilder sb = new StringBuilder()
             sb.append(']')
             if (tCaseResult.getParent() != null && tCaseResult.getParent().getJUnitReportWrapper() != null) {
                 def status = tCaseResult.getParent().getJUnitReportWrapper().getTestCaseStatus(tCaseResult.getTCaseName().getId())
                 sb.append(',')
                 sb.append('"tags": ["')
                 sb.append(status)
                 sb.append('"]')
                 /*
                  * #1BC98E; green
                  * #E64759; red
                  * #9F86FF; purple
                  * #E4D836; yellow
                  */
                 if (status == 'FAILED') {
                     sb.append(',')
                     sb.append('"backColor": "#E4D836"')
                 } else if (status == 'ERROR') {
                     sb.append(',')
                     sb.append('"backColor": "#E64759"')
                 }
             }
             sb.append('}')
             pw_.print(sb.toString())
             pw_.flush()
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult visitMaterial(Material material) {
             StringBuilder sb = new StringBuilder()
             if (materialsCount > 0) {
                 sb.append(',')
             }
             materialsCount += 1
             sb.append('{')
             sb.append('"text":"' + Helpers.escapeAsJsonText(material.getIdentifier())+ '",')
             sb.append('"selectable":true,')
             if (material.getPath().getFileName().toString().endsWith('FAILED.png')) {
                 sb.append('"backColor": "#9F86FF",')
             }
             sb.append('"href":"#' + material.hashCode() + '"')
             sb.append('}')
             pw_.print(sb.toString())
             pw_.flush()
             return RepositoryVisitResult.SUCCESS
         }
         @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
             throw new UnsupportedOperationException("failed visiting " + material.toString())
         }
    }

}
