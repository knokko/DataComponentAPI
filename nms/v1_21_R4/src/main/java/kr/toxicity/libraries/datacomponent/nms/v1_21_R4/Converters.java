package kr.toxicity.libraries.datacomponent.nms.v1_21_R4;

import io.papermc.paper.adventure.PaperAdventure;
import kr.toxicity.libraries.datacomponent.api.Converter;
import kr.toxicity.libraries.datacomponent.api.wrapper.CompoundTag;
import kr.toxicity.libraries.datacomponent.api.wrapper.Tag;
import kr.toxicity.libraries.datacomponent.api.wrapper.*;
import net.kyori.adventure.text.Component;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class Converters {
    static final Converter<Integer, Integer> INTEGER = Converter.of(i -> i, i -> i);
    static final Converter<Boolean, Boolean> BOOL = Converter.of(b -> b, b -> b);
    static final Converter<Component, net.minecraft.network.chat.Component> COMPONENT = Converter.of(
            PaperAdventure::asVanilla,
            PaperAdventure::asAdventure
    );

    @SuppressWarnings("all")
    private static class TagValueGetter implements TagVisitor {

        private Object object;

        @Override
        public void visitString(StringTag element) {
            object = element.value();
        }

        @Override
        public void visitByte(ByteTag element) {
            object = element.byteValue();

        }

        @Override
        public void visitShort(ShortTag element) {
            object = element.shortValue();

        }

        @Override
        public void visitInt(IntTag element) {
            object = element.intValue();

        }

        @Override
        public void visitLong(LongTag element) {
            object = element.longValue();

        }

        @Override
        public void visitFloat(FloatTag element) {
            object = element.floatValue();

        }

        @Override
        public void visitDouble(DoubleTag element) {
            object = element.doubleValue();
        }

        @Override
        public void visitByteArray(ByteArrayTag element) {
            object = element.getAsByteArray();
        }

        @Override
        public void visitIntArray(IntArrayTag element) {
            object = element.getAsIntArray();
        }

        @Override
        public void visitLongArray(LongArrayTag element) {
            object = element.getAsLongArray();
        }

        @Override
        public void visitList(ListTag element) {
            var list = new ArrayList<>();
            element.stream().forEach(t -> {
                var visitor = new TagValueGetter();
                t.accept(visitor);
                list.add(visitor.object);
            });
            object = list;
        }

        @Override
        public void visitCompound(net.minecraft.nbt.CompoundTag compound) {
            object = compound;
        }

        @Override
        public void visitEnd(EndTag element) {
            object = element;
        }
    }
    static final Converter<CompoundTag, net.minecraft.nbt.CompoundTag> COMPOUND_TAG = new CompoundTagConverter();
    private static class CompoundTagConverter implements Converter<CompoundTag, net.minecraft.nbt.CompoundTag> {
        private Tag<?> convert(net.minecraft.nbt.Tag tag) {
            var getter = new TagValueGetter();
            tag.accept(getter);
            var object = getter.object;
            if (object instanceof Byte value) return new ValueTag<>(ValueTag.BYTE, value);
            if (object instanceof Short value) return new ValueTag<>(ValueTag.SHORT, value);
            if (object instanceof Integer value) return new ValueTag<>(ValueTag.INT, value);
            if (object instanceof Long value) return new ValueTag<>(ValueTag.LONG, value);
            if (object instanceof UUID value) return new ValueTag<>(ValueTag.UUID, value);
            if (object instanceof Float value) return new ValueTag<>(ValueTag.FLOAT, value);
            if (object instanceof Double value) return new ValueTag<>(ValueTag.DOUBLE, value);
            if (object instanceof String value) return new ValueTag<>(ValueTag.STRING, value);
            if (object instanceof byte[] value) return new ValueTag<>(ValueTag.BYTE_ARRAY, value);
            if (object instanceof int[] value) return new ValueTag<>(ValueTag.INT_ARRAY, value);
            if (object instanceof long[] value) return new ValueTag<>(ValueTag.LONG_ARRAY, value);
            if (object instanceof List<?> value) {
                var list = new ArrayList<Tag<?>>();
                for (Object o : value) {
                    if (o instanceof net.minecraft.nbt.Tag tag1) {
                        list.add(convert(tag1));
                    }
                }
                return new ValueTag<>(ValueTag.LIST, list);
            }
            if (object instanceof net.minecraft.nbt.CompoundTag tag1) return asWrapper(tag1);
            if (object instanceof EndTag) return UnitTag.INSTANCE;
            return null;
        }
        private net.minecraft.nbt.Tag convert(Tag<?> tag) {
            var object = tag.value();
            if (object instanceof Byte value) return ByteTag.valueOf(value);
            if (object instanceof Short value) return ShortTag.valueOf(value);
            if (object instanceof Integer value) return IntTag.valueOf(value);
            if (object instanceof Long value) return LongTag.valueOf(value);
            if (object instanceof UUID value) return  new IntArrayTag(UUIDUtil.uuidToIntArray(value));
            if (object instanceof Float value) return FloatTag.valueOf(value);
            if (object instanceof Double value) return DoubleTag.valueOf(value);
            if (object instanceof String value) return StringTag.valueOf(value);
            if (object instanceof byte[] value) return new ByteArrayTag(value);
            if (object instanceof int[] value) return new IntArrayTag(value);
            if (object instanceof long[] value) return new LongArrayTag(value);
            if (object instanceof List<?> value) {
                var list = new ArrayList<net.minecraft.nbt.Tag>();
                for (Object o : value) {
                    if (o instanceof Tag<?> tag1) {
                        list.add(convert(tag1));
                    }
                }
                return new ListTag(list);
            }
            if (object instanceof CompoundTag tag1) {
                System.out.println(tag1);
                return asVanilla(tag1);
            }
            if (object instanceof UnitTag) return EndTag.INSTANCE;
            return null;
        }
        @NotNull
        @Override
        public net.minecraft.nbt.CompoundTag asVanilla(@NotNull CompoundTag tag) {
            var newTag = new net.minecraft.nbt.CompoundTag();
            tag.tags().forEach((k, v) -> {
                var get = convert(v);
                if (get != null) newTag.put(k, get);
            });
            return newTag;
        }

        @Override
        public @NotNull CompoundTag asWrapper(net.minecraft.nbt.@NotNull CompoundTag compoundTag) {
            var map = new HashMap<String, Tag<?>>();
            compoundTag.keySet().forEach(k -> {
                var get = compoundTag.get(k);
                if (get != null) map.put(k, convert(get));
            });
            return new CompoundTag(map);
        }
    }

    static final Converter<String, ResourceLocation> RESOURCE_LOCATION = Converter.of(
            s -> ResourceLocation.tryBuild("minecraft", s),
            ResourceLocation::getPath
    );

    static final Converter<ItemLore, net.minecraft.world.item.component.ItemLore> ITEM_LORE = Converter.of(
            l -> new net.minecraft.world.item.component.ItemLore(
                    l.lines().stream().map(COMPONENT::asVanilla).toList(),
                    l.styledLines().stream().map(COMPONENT::asVanilla).toList()
            ),
            l -> new ItemLore(
                    l.lines().stream().map(COMPONENT::asWrapper).toList(),
                    l.styledLines().stream().map(COMPONENT::asWrapper).toList()
            )
    );
    static final Converter<Rarity, net.minecraft.world.item.Rarity> RARITY = Converter.of(
            r -> switch (r) {
                case COMMON -> net.minecraft.world.item.Rarity.COMMON;
                case UNCOMMON -> net.minecraft.world.item.Rarity.UNCOMMON;
                case RARE -> net.minecraft.world.item.Rarity.RARE;
                case EPIC -> net.minecraft.world.item.Rarity.EPIC;
            },
            r -> switch (r) {
                case COMMON -> Rarity.COMMON;
                case UNCOMMON -> Rarity.UNCOMMON;
                case RARE -> Rarity.RARE;
                case EPIC -> Rarity.EPIC;
            }
    );
    static final Converter<Unit, net.minecraft.util.Unit> UNIT = Converter.of(
            a -> net.minecraft.util.Unit.INSTANCE,
            a -> Unit.INSTANCE
    );
    static final Converter<DyedItemColor, net.minecraft.world.item.component.DyedItemColor> DYED_ITEM_COLOR = Converter.of(
            d -> new net.minecraft.world.item.component.DyedItemColor(d.rgb()),
            d -> new DyedItemColor(d.rgb(), true)
    );
    static final Converter<MapItemColor, net.minecraft.world.item.component.MapItemColor> MAP_ITEM_COLOR = Converter.of(
            m -> new net.minecraft.world.item.component.MapItemColor(m.rgb()),
            m -> new MapItemColor(m.rgb())
    );
    static final Converter<MapId, net.minecraft.world.level.saveddata.maps.MapId> MAP_ID = Converter.of(
            m -> new net.minecraft.world.level.saveddata.maps.MapId(m.id()),
            m -> new MapId(m.id())
    );
    static final Converter<BundleContents, net.minecraft.world.item.component.BundleContents> BUNDLE_CONTENTS = Converter.of(
            m -> new net.minecraft.world.item.component.BundleContents(m.itemStacks().stream().map(CraftItemStack::asNMSCopy).toList()),
            m -> {
                var list = new ArrayList<ItemStack>();
                for (net.minecraft.world.item.ItemStack itemStack : m.items()) {
                    list.add(CraftItemStack.asBukkitCopy(itemStack));
                }
                return new BundleContents(list);
            }
    );
    static final Converter<WritableBookContent, net.minecraft.world.item.component.WritableBookContent> WRITABLE_BOOK_CONTENT = Converter.of(
            w -> new net.minecraft.world.item.component.WritableBookContent(
                    w.pages().stream().map(f -> new net.minecraft.server.network.Filterable<>(f.raw(), f.filtered())).toList()
            ),
            w -> new WritableBookContent(
                    w.pages().stream().map(f -> new Filterable<>(f.raw(), f.filtered())).toList()
            )
    );
    static final Converter<WrittenBookContent, net.minecraft.world.item.component.WrittenBookContent> WRITTEN_BOOK_CONTENT = Converter.of(
            w -> new net.minecraft.world.item.component.WrittenBookContent(
                    new net.minecraft.server.network.Filterable<>(w.title().raw(), w.title().filtered()),
                    w.author(),
                    w.generation(),
                    w.pages().stream().map(f -> new net.minecraft.server.network.Filterable<>(COMPONENT.asVanilla(f.raw()), f.filtered().map(COMPONENT::asVanilla))).toList(),
                    w.resolved()
            ),
            w -> new WrittenBookContent(
                    new Filterable<>(w.title().raw(), w.title().filtered()),
                    w.author(),
                    w.generation(),
                    w.pages().stream().map(f -> new Filterable<>(COMPONENT.asWrapper(f.raw()), f.filtered().map(COMPONENT::asWrapper))).toList(),
                    w.resolved()
            )
    );
    static final Converter<BlockItemStateProperties, net.minecraft.world.item.component.BlockItemStateProperties> BLOCK_STATE = Converter.of(
            s -> new net.minecraft.world.item.component.BlockItemStateProperties(new HashMap<>(s.properties())),
            s -> new BlockItemStateProperties(new HashMap<>(s.properties()))
    );

    static final Converter<CustomData, net.minecraft.world.item.component.CustomData> CUSTOM_DATA = Converter.of(
            c -> net.minecraft.world.item.component.CustomData.of(COMPOUND_TAG.asVanilla(c.tag())),
            c -> new CustomData(COMPOUND_TAG.asWrapper(c.copyTag()))
    );
}
