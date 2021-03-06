/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.queue;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * <em>Chronicle</em> (in a generic sense) is a Java project focused on building a persisted low
 * latency messaging framework for high performance and critical applications.
 *
 * <p> Using non-heap storage options <em>Chronicle</em> provides a processing environment where
 * applications does not suffer from <em>GarbageCollection</em>. <em>GarbageCollection</em> (GC) may
 * slow down your critical operations non-deterministically at any time.. In order to avoid
 * non-determinism and escape from GC delays off-heap memory solutions are addressed. The main idea
 * is to manage your memory manually so does not suffer from GC. Chronicle behaves like a management
 * interface over off-heap memory so you can build your own solutions over it.
 *
 * <p><em>Chronicle</em> uses RandomAccessFiles while managing memory and this choice brings lots of
 * possibility. Random access files permit non-sequential, or random, access to a file's contents.
 * To access a file randomly, you open the file, seek a particular location, and read from or
 * writeBytes to that file. RandomAccessFiles can be seen as "large" C-type byte arrays that you can
 * access any random index "directly" using pointers. File portions can be used as ByteBuffers if
 * the portion is mapped into memory.
 *
 * <p>{@link ChronicleQueue} (now in the specific sense) is the main interface for management and
 * can be seen as the "Collection class" of the <em>Chronicle</em> environment. You will reserve a
 * portion of memory and then put/fetch/update records using the {@link ChronicleQueue}
 * interface.</p>
 *
 * <p>{@link Excerpt} is the main data container in a {@link ChronicleQueue}, each Chronicle is
 * composed of Excerpts. Putting data to a queue means starting a new Excerpt, writing data into it
 * and finishing the Excerpt at the upper.</p>
 *
 * <p>While {@link Excerpt} is a generic purpose container allowing for remote access, it also has
 * more specialized counterparts for sequential operations. See {@link ExcerptTailer} and {@link
 * net.openhft.chronicle.queue.ExcerptAppender}</p>
 *
 * @author peter.lawrey
 */
public interface ChronicleQueue extends Closeable {
    int TEST_BLOCK_SIZE = 256 * 1024; // smallest safe block size for Windows 8+

    /**
     * @return a new ExcerptTailer to read sequentially.
     */
    @NotNull
    ExcerptTailer createTailer();

    /**
     * An Appender can be used to writeBytes new excerpts sequentially to the upper.
     *
     * @return A thread local Appender for writing new entries to the end.
     */
    @NotNull
    ExcerptAppender acquireAppender();

    /**
     * @deprecated to be remove in version 4.6 or later use {@link ChronicleQueue#acquireAppender()}
     */
    @Deprecated
    default ExcerptAppender createAppender() {
        return acquireAppender();
    }

    /**
     * @return the lowest valid index available, or Long.MAX_VALUE if none are found
     */
    long firstIndex();

    /**
     * @return the type of wire used, for example WireTypes.TEXT or WireTypes.BINARY
     */
    @NotNull
    WireType wireType();

    /**
     * Remove all the entries in the queue.
     */
    void clear();

    /**
     * @return the base path where ChronicleQueue stores its data.
     */
    @NotNull
    File file();

    /**
     * Dump a Queue in YAML format.
     *
     * @return the contents of the Queue as YAML.
     */
    String dump();

    /**
     * Dump a range of entries to a Writer
     *
     * @param writer    to write to
     * @param fromIndex first index to include
     * @param toIndex   last index to include.
     */
    void dump(Writer writer, long fromIndex, long toIndex);

    default void dump(OutputStream stream, long fromIndex, long toIndex) {
        dump(new OutputStreamWriter(stream, StandardCharsets.UTF_8), fromIndex, toIndex);
    }

    int indexCount();

    int indexSpacing();

    int sourceId();

    RollCycle rollcycle();
}
