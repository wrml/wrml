import org.wrml.contrib.runtime.service.groovy.GroovyTemplate
import org.wrml.model.Model
import org.wrml.runtime.Context
import org.wrml.runtime.Dimensions
import org.wrml.runtime.Keys

class Hello implements GroovyTemplate {
    def Model fill(Context context, Model model, Keys keys, Dimensions dimensions) {
        model.setSecondsToLive(8L)
        return model
    }
}
