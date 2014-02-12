class Server implements org.wrml.contrib.runtime.service.groovy.GroovyServiceInterface {
    def callCount = 0;
    def cache = new java.util.HashMap()

    // Introspection Methods
    def getCallCount() {
        return callCount
    }

    def getCache() {
        return cache
    }

    // Service methods
    def void delete(org.wrml.runtime.Context context, org.wrml.runtime.Keys keys) {
        callCount++
        def keyURIs = keys.getKeyedSchemaUris()

        for (uri in keyURIs) {
            def key = keys.getValue(uri)
            cache.remove(key)
        }
    }

    //def get(model, keys, dimensions)
    def org.wrml.model.Model get(org.wrml.runtime.Context context, org.wrml.model.Model model, org.wrml.runtime.Keys keys, org.wrml.runtime.Dimensions dimensions) {
        callCount++
        def key = keys.getValue(model.getSchemaUri())

        if (cache.containsKey(key)) {
            return cache[key]
        }

        return model
    }

    def void init(java.util.SortedMap map) {
        println "Doing something awesome..."
    }

    def save(model) {
        callCount++
        def keys = model.getKeys()
        def key = keys.getValue(model.getSchemaUri())
        cache[key] = model
        return model
    }
}
