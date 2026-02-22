package de.sw1ld;

import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FileIdMesgListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class FileIdListener implements FileIdMesgListener {

  private LocalDateTime timeCreated;

  @Override
  public void onMesg(FileIdMesg mesg) {
    if (mesg.getTimeCreated() != null) {
      this.timeCreated = convertToLocalDateTime(mesg.getTimeCreated().getDate());
    }
  }

  private LocalDateTime convertToLocalDateTime(Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public LocalDateTime getTimeCreated() {
    return timeCreated;
  }
}
