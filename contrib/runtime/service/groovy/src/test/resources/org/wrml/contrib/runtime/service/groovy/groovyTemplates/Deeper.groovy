package org.wrml.contrib.runtime.service.groovy.groovyTemplates

class Deeper {
    long count = 0L

    long getAndIncCount() {
        return count++
    }
}