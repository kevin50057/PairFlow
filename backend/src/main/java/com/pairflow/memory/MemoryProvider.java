package com.pairflow.memory;

import com.pairflow.home.dto.HomeMemory;

/**
 * Seam for the "去年今天" home card. The Album module supplies the real
 * implementation; until then a no-op bean returns null so the home dashboard
 * works without photos.
 */
public interface MemoryProvider {

    HomeMemory onThisDay(String coupleId, String viewerId);
}
