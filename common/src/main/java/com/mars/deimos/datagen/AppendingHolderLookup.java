package com.mars.deimos.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AppendingHolderLookup<T> implements HolderLookup<T> {
    private final HolderLookup<T> parent;
    private final List<Holder.Reference<T>> extra;
    private final Map<ResourceKey<T>, Holder.Reference<T>> extraByKey;

    public AppendingHolderLookup(HolderLookup<T> parent, List<Holder.Reference<T>> extra) {
        this.parent = parent;
        this.extra = extra;
        this.extraByKey = extra.stream().collect(Collectors.toMap(Holder.Reference::key, r -> r));
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return Stream.concat(parent.listElements(), extra.stream());
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return parent.listTags();
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
        Holder.Reference<T> ref = extraByKey.get(key);
        return ref != null ? Optional.of(ref) : parent.get(key);
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> tag) {
        return parent.get(tag);
    }
}