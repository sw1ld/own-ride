package de.sw1ld;

import java.util.List;

public record ResponseFragment(List<FeedItem> items, String nextEncodedCursor) {}
