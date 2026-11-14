package pl.kaitoudev.tabkeeper.gateway.domain.group;

/** Invitation states, including absent and unknown upstream values. */
public enum InvitationStatus {
  UNSPECIFIED,
  PENDING,
  ACCEPTED,
  DECLINED,
  REVOKED,
  UNRECOGNIZED
}
