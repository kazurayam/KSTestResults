package com.kazurayam.materials.view

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers

import spock.lang.Specification

class ExecutionPropertiesWrapperSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(ExecutionPropertiesWrapperSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(ExecutionPropertiesWrapperSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetExecutionProfile() {
        setup:
        Path p = workdir_.resolve('Reports/main/TS1/20180805_081908/execution.properties')
        ExecutionPropertiesWrapper epw = new ExecutionPropertiesWrapper(p)
        when:
        def executionProfile = epw.getExecutionProfile()
        then:
        executionProfile.toString() == 'default'
    }

}
