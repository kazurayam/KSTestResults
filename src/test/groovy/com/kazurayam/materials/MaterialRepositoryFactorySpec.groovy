package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification
//@Ignore
class MaterialRepositoryFactorySpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryFactorySpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialRepositoryFactorySpec.class)}")
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
        Path materials = workdir_.resolve('Materials')
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.markAsCurrent('Test Suites/TS1')
        mr.ensureDirectoryOf('Test Suites/TS1')
        then:
        mr != null
        mr.toString().contains('TS1')
    }

    // helper methods

}
