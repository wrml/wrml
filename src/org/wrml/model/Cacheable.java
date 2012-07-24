
package org.wrml.model;

public interface Cacheable extends Model {

    /**
     * Gets the string representation of the model's representational state
     * version.
     * 
     * @return The "entity tag" string for this instance.
     */
    public String getCacheTag();

    /**
     * As a representation of some server-owned resource's state, a models's
     * field values may change at any time (within the system of record). This
     * method returns the number of seconds that it is safe to cache this
     * models's representation without concern for changes taking place at its
     * resource origin.
     * 
     * @return The number of seconds that this model's representation may
     *         be considered "fresh".
     */
    public Long getSecondsToLive();

    public void setCacheTag(String tag);

    public void setSecondsToLive(Long secondsToLive);
}
