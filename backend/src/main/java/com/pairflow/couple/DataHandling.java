package com.pairflow.couple;

/** What to do with shared data when a couple unbinds (spec 7.1 / 9.3). */
public enum DataHandling {
    ARCHIVE,        // keep shared space read-only (default)
    DELETE,         // delete shared data
    KEEP_PERSONAL,  // each keeps a personal copy
    EXPORT          // export before ending
}
