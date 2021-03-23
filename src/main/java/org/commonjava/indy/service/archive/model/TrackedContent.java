package org.commonjava.indy.service.archive.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

public class TrackedContent
        implements Externalizable
{

    private TrackingKey key;

    private Set<TrackedContentEntry> uploads;

    private Set<TrackedContentEntry> downloads;

    public TrackedContent(){}

    public void setKey(TrackingKey key) {
        this.key = key;
    }

    public void setUploads(Set<TrackedContentEntry> uploads) {
        this.uploads = uploads;
    }

    public void setDownloads(Set<TrackedContentEntry> downloads) {
        this.downloads = downloads;
    }

    public TrackedContent(final TrackingKey key, final Set<TrackedContentEntry> uploads,
                          final Set<TrackedContentEntry> downloads )
    {
        this.key = key;
        this.uploads = uploads;
        this.downloads = downloads;
    }

    public TrackingKey getKey()
    {
        return key;
    }

    public Set<TrackedContentEntry> getUploads()
    {
        return uploads;
    }

    public Set<TrackedContentEntry> getDownloads()
    {
        return downloads;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrackedContent ) )
        {
            return false;
        }

        TrackedContent that = (TrackedContent) o;

        return getKey() != null ? getKey().equals( that.getKey() ) : that.getKey() == null;

    }

    @Override
    public int hashCode()
    {
        return getKey() != null ? getKey().hashCode() : 0;
    }

    @Override
    public void writeExternal( ObjectOutput objectOutput )
            throws IOException
    {
        objectOutput.writeObject( key );
        objectOutput.writeObject( uploads );
        objectOutput.writeObject( downloads );
    }

    @Override
    public void readExternal( ObjectInput objectInput )
            throws IOException, ClassNotFoundException
    {
        key = (TrackingKey) objectInput.readObject();
        Set<TrackedContentEntry> ups = (Set<TrackedContentEntry>) objectInput.readObject();
        uploads = ups == null ? new HashSet<>() : new HashSet<>( ups );

        Set<TrackedContentEntry> downs = (Set<TrackedContentEntry>) objectInput.readObject();
        downloads = downs == null ? new HashSet<>() : new HashSet<>( downs );
    }
}