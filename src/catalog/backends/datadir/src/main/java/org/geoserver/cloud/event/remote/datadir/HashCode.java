/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.remote.datadir;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.geoserver.catalog.Info;

import java.nio.charset.StandardCharsets;

@EqualsAndHashCode
public final class HashCode {

    private static final InfoHasher hasher = new InfoHasher();

    private final com.google.common.hash.HashCode hash;

    public HashCode() {
        this(newHasher().hash());
    }

    private HashCode(com.google.common.hash.HashCode hash) {
        this.hash = hash;
    }

    public static HashCode valueOf(@NonNull Info info) {
        return hasher.hash(info);
    }

    public HashCode xor(@NonNull Info info) {
        return xor(valueOf(info));
    }

    com.google.common.hash.HashCode hash() {
        return hash;
    }

    byte[] asBytes() {
        return hash.asBytes();
    }

    @Override
    public String toString() {
        return hash.toString();
    }

    public HashCode xor(@NonNull HashCode other) {
        byte[] xor = hash.asBytes();
        final int length = xor.length;
        byte[] buff = other.asBytes();
        for (int x = 0; x < length; x++) {
            xor[x] = (byte) (xor[x] ^ buff[x]);
        }
        return HashCode.fromBytes(xor);
    }

    public HashCode remove(@NonNull String id) {
        Hasher builder = newHasher();
        builder.putLong(Long.MIN_VALUE);
        builder.putString(id, StandardCharsets.UTF_8);
        return this.xor(valueOf(builder.hash()));
    }

    static HashCode fromBytes(byte[] bytes) {
        return valueOf(com.google.common.hash.HashCode.fromBytes(bytes));
    }

    static HashCode valueOf(com.google.common.hash.HashCode hash) {
        return new HashCode(hash);
    }

    static Hasher newHasher() {
        return Hashing.sha256().newHasher();
    }
}
