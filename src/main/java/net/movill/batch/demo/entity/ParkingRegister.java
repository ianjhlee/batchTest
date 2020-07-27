package net.movill.batch.demo.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ParkingRegister {
  @Id
  private Long idx;
  private Long aptIdx;
  private String dong;
  private String ho;
  private String carNumber;
  private String carName;
  private String carOwnerName;
  private String carOwnerContact;
  private String status;
  private String memo;
  private String regDate;
  private String modDate;

}
