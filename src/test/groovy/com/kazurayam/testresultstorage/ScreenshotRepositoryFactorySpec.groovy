package com.kazurayam.testresultstorage

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification
//@Ignore
class ScreenshotRepositoryFactorySpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(ScreenshotRepositoryFactorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        setup:
        String dirName = 'testCreateInstance'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        when:
        ScreenshotRepository scRepo = ScreenshotRepositoryFactory.createInstance(workdir, 'Test Suites/TS1')
        then:
        scRepo != null
        scRepo.toString().contains('TS1')
    }

    // helper methods

}
