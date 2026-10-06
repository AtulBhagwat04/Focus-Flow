---
name: platform-compat
description: Use when a change touches Android permissions, foreground services, overlays, usage access, notifications, audio, layouts, background work, OEM battery behaviour, target SDK, or Play policy.
---

# Platform compatibility

## Before changing anything
Read `docs/DEVICE_COMPATIBILITY.md` and `docs/RELEASE_AND_COMPLIANCE.md`. Android behaviour and Play policy change every year, so verify current documentation instead of relying on memory, and record the date of verification.

## Checklist per change
1. Which Android versions does this behave differently on? Check minimum, 13, 14, 15, 16 and 17.
2. Does it need a new permission, foreground service type, or declaration? If yes, stop and request an ADR and owner approval.
3. Does it work with battery saver, Doze, and restricted background modes?
4. Does it survive process death and reboot?
5. Does it behave on large screens, foldables, multi-window and picture-in-picture?
6. Does it work with secondary users and work profiles?
7. Does it work when each permission is missing or revoked?
8. Is Advanced Protection Mode handled if accessibility is involved?
9. Is audio started from a context allowed on Android 17?
10. Does it need an OEM-specific guide or test?

## Output
List the affected versions, the verification sources and dates, the handling chosen, the tests to run, and any documentation to update.

## Pitfalls
Assuming Pixel behaviour matches all OEMs, ignoring restricted settings for sideloaded test builds, requesting broad permissions that Play restricts, starting foreground services from disallowed contexts, ignoring target SDK behaviour changes until the deadline.
