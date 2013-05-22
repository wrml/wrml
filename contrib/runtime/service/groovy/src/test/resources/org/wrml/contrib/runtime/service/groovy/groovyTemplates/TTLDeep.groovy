import org.wrml.model.Model
import org.wrml.runtime.Context
import org.wrml.runtime.Dimensions
import org.wrml.runtime.Keys
import org.wrml.contrib.runtime.service.groovy.GroovyTemplate
import org.wrml.contrib.runtime.service.groovy.groovyTemplates.Deeper

class TTLDeeper implements GroovyTemplate
{
    def deeper

    TTLDeeper()
    {
        deeper = new Deeper()
    }

    def Model fill(Context context, Model model, Keys keys, Dimensions dimensions)
    {
        model.setSecondsToLive(deeper.getAndIncCount())
        return model
    }
}