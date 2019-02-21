package com.kazurayam.materials

import java.nio.file.Path

/**
 * MaterialStorage is an external directory for Materials outside a Katalon Studio project.
 * You can backup your Materials from the project's Materials directory to the MaterialStorage.
 * Also you can restore a version of TSuiteExecution 
 * 
 * @author kazurayam
 */
interface MaterialStorage {
    
    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp 
     * from the project's Materials folder (fromMR) into this Material Storage.
     * 
     * @param fromMR
     * @param tSuiteResultId
     * @param scan whether to call this.scan after backing up this TSuiteResultId
     * @return number of Material files transfered
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, TSuiteResultId tSuiteResultId, boolean scan) throws IOException

    /**
     * copy a set of Material files identified by a pair of a tSuiteName and a RetrievalBy object
     * from the project's Materials folder (fromMR) into this Material Storage.
     * 
     * @param fromMR
     * @param tSuiteName
     * @param retrievalBy
     * @return number of Material files transfered
     * @throws IOException
     */
    int backup(MaterialRepository fromMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException

    /**
     * copy all Material files in the project's Materials folder (fromMR)
     * at the timing of invokation into this Material Storage
     *
     * @param fromMR
     * @return
     * @throws IOException
     */
    int backup(MaterialRepository fromMR) throws IOException
    
    /**
     * delete all of subdirectories and material files which belogns to 
     * the tSuiteName + tSuiteTimestamp in this Material Storage.
     * Will remove the tSuiteTimestamp directory, but retain the tSuiteName directory
     *  
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return number of Material files deleted. number of deleted directories are not included.
     */
    int clear(TSuiteResultId tSuiteResultId) throws IOException

    int clear(List<TSuiteResultId> tSuiteResultIdList) throws IOException

    /**
     * delete all of subdirectories and material files which belong to
     * the tSuiteName in this Material Storage.
     * Will remove the tSuiteName directory.
     * 
     * @param tSuiteName
     * @return number of Material files deleted. number of deleted directories are not included.
     * @throws IOException
     */
    int clear(TSuiteName tSuiteName) throws IOException
    
    /**
     * delete all Material files in this Material Storage, keep the baseDir undeleted.
     * 
     * @return number of Matrial files deleted
     * @throws IOException
     */
    void empty() throws IOException

    /**
     * 
     * @return the baseDir Path
     */
    Path getBaseDir()
    
    /**
     * 
     * @return sum of the size of Material files contained in the Storage 
     */
    long getSize()
    
    /**
     * 
     * @param tSuiteName
     * @return
     */
    Set<Path> getSetOfMaterialPathRelativeToTSuiteTimestamp(TSuiteName tSuiteName)
    
    /**
     * 
     * @return
     */
    List<TSuiteName> getTSuiteNameList()
    
    /**
     * 
     * @param tSuiteName
     * @param tSuiteTimestamp
     * @return
     */
    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId)
    
    
    /**
     * 
     * @param tSuiteName
     * @return
     */
    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName)
    
    /**
     * 
     * @return
     */
    List<TSuiteResultId> getTSuiteResultIdList()
    
    /**
     * 
     * @param tSuiteName
     * @return
     */
    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList)
    
    
    /**
     * 
     */
    List<TSuiteResult> getTSuiteResultList()
    
    /**
     * list the current status of the Storage
     * 
     * @param options
     * @return a string containing lines which includes TSuiteName, TSuiteTimestamp, sum of file size
     */
    void list(Writer output, Map options)
    
    /**
     * Calcute the total file size in the Storage to check if it exceeds the target size in bytes.
     * All of TSuiteNames are inspected as possible deletion target.
     * If exceeding, clear older TSuiteResults to reduce the total less than the target.
     * 
     * e.g., MaterialStorage.reduceSizeTo(20 * 1000 * 1000 * 1000); // 20 giga-bytes
     * 
     * @param targetBytes want to make the Storage size less than this
     * @return the size of the Storage after reduction = sum of remaining Material files
     */
    long reduce(long targetBytes) throws IOException
    
    /**
     * copy a set of Material files idenfified by a pair of a tSuiteName and a specific tSuiteTimestamp
     * from this Material Storage into the project's Materials folder (intoMR).
     *
     * @param intoMR
     * @param tSuiteResultId
     * @param scan wither to call scan() after restoring this TSuiteResultId
     * @return number of Material files transfered
     * @throws IOException
     */
    int restore(MaterialRepository intoMR, TSuiteResultId tSuiteResultId, boolean scan) throws IOException
    
    /**
     * copy a set of Material files identified by a pair of a tSuiteName and a RetrievalBy object
     * from this Material Storage into the project's Materials folder (intoMR).
     *
     * @param intoMR
     * @param tSuiteName
     * @param retrievalBy
     * @return number of Material files transfered
     * @throws IOException
     */
    int restore(MaterialRepository intoMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException
    
    /**
     * Retrieve a single TSuiteResult out of the MaterialStorage by the retrievalBy, then
     * copy the Material files of the TSuiteResult from MaterialStorage into MaterialRespository.
     * 
     * synonym to unitaryRestore() mehtod
     * 
     * @param intoMR
     * @param retrievalBy
     * @return
     * @throws IOException
     */
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
                                RetrievalBy retrievalBy) throws IOException
    
    int restoreUnary(MaterialRepository intoMR, TSuiteName tSuiteName,
                                RetrievalBy retrievalBy) throws IOException
    
    int restoreCollective(MaterialRepository intoMR, TSuiteName tSuiteName,
                                RetrievalBy retrievalBy) throws IOException

}