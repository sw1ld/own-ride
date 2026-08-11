package de.sw1ld;

import java.util.List;

public record Fragment(List<Activity> activities, Cursor nextCursor) {}
