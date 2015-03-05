package com.tinkerpop.gremlin.process.computer.util;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.util.TraversalHelper;
import com.tinkerpop.gremlin.util.Serializer;
import org.apache.commons.configuration.Configuration;

import java.io.IOException;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexProgramHelper {

    public static void serialize(final Object object, final Configuration configuration, final String key) throws IOException {
        configuration.setProperty(key, Serializer.serializeObject(object));
    }

    public static <T> T deserialize(final Configuration configuration, final String key) throws IOException, ClassNotFoundException {
        final List byteList = configuration.getList(key);
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = Byte.valueOf(byteList.get(i).toString().replace("[", "").replace("]", ""));
        }
        return (T) Serializer.deserializeObject(bytes);
    }

    public static void verifyReversibility(final Traversal traversal) {
        if (!TraversalHelper.isReversible(traversal))
            throw new IllegalArgumentException("The provided traversal is not reversible");
    }

    public static void legalConfigurationKeyValueArray(final Object... configurationKeyValues) throws IllegalArgumentException {
        if (configurationKeyValues.length % 2 != 0)
            throw new IllegalArgumentException("The provided arguments must have a size that is a factor of 2");
        for (int i = 0; i < configurationKeyValues.length; i = i + 2) {
            if (!(configurationKeyValues[i] instanceof String))
                throw new IllegalArgumentException("The provided key/value array must have a String key on even array indices");
        }
    }
}
