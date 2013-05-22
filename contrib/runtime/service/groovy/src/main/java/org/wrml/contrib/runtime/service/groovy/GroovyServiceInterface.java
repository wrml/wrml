
package org.wrml.contrib.runtime.service.groovy;

import java.util.SortedMap;
import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;

public interface GroovyServiceInterface 
{
    
    public void delete(Context context, Keys keys);
    
    public <M extends Model> M get(Context context, M model, Keys keys, Dimensions dimensions);
    
    public <M extends Model> M save(M model);
}
