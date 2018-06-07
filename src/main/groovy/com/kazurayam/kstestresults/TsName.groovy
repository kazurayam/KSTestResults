package com.kazurayam.kstestresults

class TsName {

    private String value

    TsName(String testSuiteId) {
        this.value = testSuiteId.replaceFirst('^Test Suites/', '')
    }

    String getValue() {
        return value
    }

    // -------------------- overriding Object properties ----------------------
    @Override
    String toString() {
        return value
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TsName))
            return false
        TsName other = (TsName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }
}