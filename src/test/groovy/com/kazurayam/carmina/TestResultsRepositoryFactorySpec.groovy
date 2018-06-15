package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification
//@Ignore
class TestResultsRepositoryFactorySpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TestResultsRepositoryFactorySpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsRepositoryFactorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        when:
        TestResultsRepository trr = TestResultsRepositoryFactory.createInstance(workdir_)
        trr.setCurrentTestSuite('Test Suites/TS1')
        then:
        trr != null
        trr.toString().contains('TS1')
    }

    // helper methods

}
