package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.regex.Pattern

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.TSuiteTimestampImpl

import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
//@Ignore
class HelpersSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(HelpersSpec.class)

    // fields
    private static Path workdir_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(HelpersSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    /**
     *
     * @return
     */
    def testEnsureDirs() {
        setup:
        Path subdir = workdir_.resolve('testEnsureDirs')
        when:
        Helpers.ensureDirs(subdir)
        then:
        subdir.toFile().exists()
        cleanup:
        Helpers.deleteDirectory(subdir)
    }

    def testTouch() {
        setup:
        Path subdir = workdir_.resolve('testTouch')
        Helpers.ensureDirs(subdir)
        Path file = subdir.resolve('dummy')
        when:
        Helpers.touch(file)
        then:
        file.toFile().exists()
        cleanup:
        Helpers.deleteDirectory(subdir)
    }

    def testDeleteDirectory() {
        setup:
        Path subdir = workdir_.resolve('testDeleteDirectory')
        Helpers.ensureDirs(subdir)
        Path file = subdir.resolve('dummy')
        Helpers.touch(file)
        when:
        Helpers.deleteDirectory(subdir)
        then:
        !subdir.toFile().exists()
    }

    def testDeleteDirectoryContents() {
        setup:
        Path subdir = workdir_.resolve('testDeleteDirectoryContents')
        Helpers.ensureDirs(subdir)
	Path dummy = subdir.resolve('dummy')
        Helpers.touch(dummy)
	Path subsubdir = subdir.resolve('subsubdir')
	Helpers.ensureDirs(subsubdir)
	Path dummy1 = subsubdir.resolve('dummy1')
        Helpers.touch(dummy1)
        when:
        Helpers.deleteDirectoryContents(subdir)
        then:
        !dummy1.toFile().exists()
	!dummy.toFile().exists()
	!subsubdir.toFile().exists()
        subdir.toFile().exists()
    }

    def testGetTimestampAsString() {
        setup:
        //Pattern pattern = Pattern.compile('[12][0-9]{3}[01][0-9][0-5][0-9]_[012][0-5][0-5][0-9]') // 20180529_110342
        Pattern pattern = Pattern.compile('[0-9]{8}_[0-9]{6}')
        when:
        String tstamp = Helpers.getTimestampAsString(LocalDateTime.now())
        then:
        pattern.matcher(tstamp).matches()
    }

    def testCopyDirectory() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Materials')
        Path targetDir = workdir_.resolve('testCopyDirectory')
        when:
        Helpers.copyDirectory(sourceDir, targetDir)
        then:
        Files.exists(targetDir.resolve('main.TS1'))
        Files.exists(targetDir.resolve('main.TS1/20180530_130419'))
        Files.exists(targetDir.resolve('main.TS1/20180530_130419/main.TC1'))
        Files.exists(targetDir.resolve('main.TS1/20180530_130419/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        Files.exists(targetDir.resolve('main.TS1/20180530_130604'))
        Files.exists(targetDir.resolve('main.TS1/20180530_130604/main.TC1'))
        Files.exists(targetDir.resolve('main.TS1/20180530_130604/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
    }

    def testGetClassShortName() {
        expect:
        Helpers.getClassShortName(Helpers.class) == 'Helpers'
    }

    def testEscapeAsJsonText_reversesolidus() {
        expect:
        Helpers.escapeAsJsonText('\\') == '\\\\'
    }

    def testEscapeAsJsonText_quotationmark() {
        expect:
        Helpers.escapeAsJsonText('"') == '\\"'
    }

    def testExcapeAsJsonText_newline() {
        expect:
        Helpers.escapeAsJsonText('\n') == '\\n'
    }

    def testExcapeAsJsonText_carriagereturn() {
        expect:
        Helpers.escapeAsJsonText('\r') == '\\r'
    }

    def testExcapeAsJsonText_tab() {
        expect:
        Helpers.escapeAsJsonText('\t') == '\\t'
    }


    def testNow() {
        when:
        String now = Helpers.now()
        LocalDateTime ldt = TSuiteTimestampImpl.parse(now)
        logger_.debug("#testNow now=${now},ldt=${ldt}")
        then:
        true
    }



    // helper methods
    private boolean someHelper() {
        return true
    }
}
