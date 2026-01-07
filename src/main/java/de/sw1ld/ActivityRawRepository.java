package de.sw1ld;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRawRepository {

  Optional<ActivityRaw> findById(UUID id);
}
