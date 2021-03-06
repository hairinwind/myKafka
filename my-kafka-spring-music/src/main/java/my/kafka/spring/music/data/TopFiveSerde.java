package my.kafka.spring.music.data;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class TopFiveSerde implements Serde<TopFiveSongs> {
    @Override
    public void configure(final Map<String, ?> map, final boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<TopFiveSongs> serializer() {
        return new Serializer<TopFiveSongs>() {
            @Override
            public void configure(final Map<String, ?> map, final boolean b) {
            }

            @Override
            public byte[] serialize(final String s, final TopFiveSongs topFiveSongs) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final DataOutputStream
                        dataOutputStream =
                        new DataOutputStream(out);
                try {
                    for (final SongPlayCount songPlayCount : topFiveSongs) {
                        dataOutputStream.writeLong(songPlayCount.getSongId());
                        dataOutputStream.writeLong(songPlayCount.getPlays());
                    }
                    dataOutputStream.flush();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                return out.toByteArray();
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public Deserializer<TopFiveSongs> deserializer() {
        return new Deserializer<TopFiveSongs>() {
            @Override
            public void configure(final Map<String, ?> map, final boolean b) {

            }

            @Override
            public TopFiveSongs deserialize(final String s, final byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                final TopFiveSongs result = new TopFiveSongs();

                final DataInputStream
                        dataInputStream =
                        new DataInputStream(new ByteArrayInputStream(bytes));

                try {
                    while (dataInputStream.available() > 0) {
                        result.add(new SongPlayCount(dataInputStream.readLong(),
                                dataInputStream.readLong()));
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                return result;
            }

            @Override
            public void close() {

            }
        };
    }
}
