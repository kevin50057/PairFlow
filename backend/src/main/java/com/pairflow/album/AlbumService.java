package com.pairflow.album;

import com.pairflow.album.dto.AlbumResponse;
import com.pairflow.album.dto.CreateAlbumRequest;
import com.pairflow.album.dto.UpdateAlbumRequest;
import com.pairflow.common.error.ApiException;
import com.pairflow.couple.CoupleContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final CoupleContext coupleContext;

    public AlbumService(AlbumRepository albumRepository, PhotoRepository photoRepository, CoupleContext coupleContext) {
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
        this.coupleContext = coupleContext;
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> list() {
        String coupleId = coupleContext.requireCoupleId();
        return albumRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId).stream()
                .map(a -> AlbumResponse.from(a, photoRepository.countByAlbumId(a.getId())))
                .toList();
    }

    @Transactional
    public AlbumResponse create(CreateAlbumRequest req) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        Album a = new Album();
        a.setCoupleId(coupleId);
        a.setTitle(req.title().trim());
        a.setDescription(req.description());
        a.setCoverPhotoUrl(req.coverPhotoUrl());
        a.setCreatedBy(me);
        return AlbumResponse.from(albumRepository.save(a), 0);
    }

    @Transactional
    public AlbumResponse update(String id, UpdateAlbumRequest req) {
        Album a = load(id);
        if (req.title() != null) a.setTitle(req.title().trim());
        if (req.description() != null) a.setDescription(req.description());
        if (req.coverPhotoUrl() != null) a.setCoverPhotoUrl(req.coverPhotoUrl());
        return AlbumResponse.from(a, photoRepository.countByAlbumId(a.getId()));
    }

    @Transactional
    public void delete(String id) {
        Album a = load(id);
        // Detach photos rather than delete them — they remain in the couple's timeline.
        photoRepository.findByCoupleIdAndAlbumIdOrderByCreatedAtDesc(a.getCoupleId(), a.getId())
                .forEach(p -> p.setAlbumId(null));
        albumRepository.delete(a);
    }

    private Album load(String id) {
        Album a = albumRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Album not found"));
        if (!a.getCoupleId().equals(coupleContext.requireCoupleId())) {
            throw ApiException.notFound("Album not found");
        }
        return a;
    }
}
