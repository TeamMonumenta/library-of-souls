package com.playmonumenta.libraryofsouls.nbt;

import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;

// TODO: remove LegacyBookSerialize when legacy v0.1 BoS serialization is no longer needed
public final class LegacyBookSerialize {

	private static final String _dataPre = "§k";

	private LegacyBookSerialize() {
	}

	public static @Nullable String loadData(BookMeta meta, String dataTitle) {
		int pageCount = meta.getPageCount();
		if (pageCount == 0) {
			return null;
		}

		StringBuilder dataSB = new StringBuilder();
		for (int i = 1; i <= pageCount; ++i) {
			String page = meta.getPage(i);
			if (page.startsWith(dataTitle)) {
				dataSB.append(page.substring(dataTitle.length() + _dataPre.length()));
				for (++i; i <= pageCount; ++i) {
					page = meta.getPage(i);
					if (page.startsWith(_dataPre)) {
						dataSB.append(page.substring(_dataPre.length()));
					} else {
						break;
					}
				}
				return dataSB.toString();
			}
		}
		return null;
	}

	public static void saveToBook(BookMeta meta, String data, String dataTitle) {
		int max;
		int pageMax = 255 - _dataPre.length();
		for (int i = 0, l = data.length(); i < l; i += max) {
			max = (i == 0 ? pageMax - dataTitle.length() : pageMax);
			meta.addPage((i == 0 ? dataTitle : "") + _dataPre + data.substring(i, (i + max > l ? l : i + max)));
		}
	}

}
