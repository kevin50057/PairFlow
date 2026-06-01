package com.pairflow.album;

import com.pairflow.album.dto.PhotoResponse;
import com.pairflow.album.dto.UpdatePhotoRequest;
import com.pairflow.common.error.ApiException;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.CoupleContext;
import com.pairflow.home.dto.HomeMemory;
import com.pairflow.memory.MemoryProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Also supplies the home "去年今天" card via {@link MemoryProvider}. */
@Service
@Primary
public class PhotoService implements MemoryProvider {

    private final PhotoRepository photoRepository;
    private final MediaStorageService storage;
    private final CoupleContext coupleContext;

    public PhotoService(PhotoRepository photoRepository, MediaStorageService storage, CoupleContext coupleContext) {
        this.photoRepository = photoRepository;
        this.storage = storage;
        this.coupleContext = coupleContext;
    }

    @Transactional
    public PhotoResponse upload(MultipartFile file, String albumId, String caption,
                                Instant takenAt, String locationName, List<String> tags) {
        String coupleId = coupleContext.requireCoupleId();
        String me = coupleContext.currentUserId();
        String filename = storage.store(file);

        Photo p = new Photo();
        p.setCoupleId(coupleId);
        p.setUploaderId(me);
        p.setAlbumId(albumId != null && !albumId.isBlank() ? albumId : null);
        p.setStorageKey(filename);
        p.setImageUrl("/api/media/" + filename);
        p.setThumbnailUrl("/api/media/" + filename);
        p.setCaption(caption);
        p.setTakenAt(takenAt != null ? takenAt : Instant.now());
        p.setLocationName(locationName);
        p.setTagsCsv(joinTags(tags));
        return PhotoResponse.from(photoRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> list(String albumId, Boolean favorite) {
        String coupleId = coupleContext.requireCoupleId();
        List<Photo> photos;
        if (albumId != null && !albumId.isBlank()) {
            photos = photoRepository.findByCoupleIdAndAlbumIdOrderByCreatedAtDesc(coupleId, albumId);
        } else if (Boolean.TRUE.equals(favorite)) {
            photos = photoRepository.findByCoupleIdAndFavoriteTrueOrderByCreatedAtDesc(coupleId);
        } else {
            photos = photoRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId);
        }
        return photos.stream().map(PhotoResponse::from).toList();
    }

    @Transactional
    public PhotoResponse update(String id, UpdatePhotoRequest req) {
        Photo p = load(id);
        if (req.albumId() != null) p.setAlbumId(req.albumId().isBlank() ? null : req.albumId());
        if (req.caption() != null) p.setCaption(req.caption());
        if (req.takenAt() != null) p.setTakenAt(req.takenAt());
        if (req.locationName() != null) p.setLocationName(req.locationName());
        if (req.tags() != null) p.setTagsCsv(joinTags(req.tags()));
        if (req.isFavorite() != null) p.setFavorite(req.isFavorite());
        return PhotoResponse.from(p);
    }

    @Transactional
    public void delete(String id) {
        photoRepository.delete(load(id));
    }

    /** "去年今天": photos taken on this month/day in a previous year (spec 7.6). */
    @Override
    @Transactional(readOnly = true)
    public HomeMemory onThisDay(String coupleId, String viewerId) {
        LocalDate today = AppTime.today();
        List<Photo> matches = photoRepository.findByCoupleId(coupleId).stream()
                .filter(p -> p.getTakenAt() != null)
                .filter(p -> {
                    LocalDate d = p.getTakenAt().atZone(AppTime.ZONE).toLocalDate();
                    return d.getMonthValue() == today.getMonthValue()
                            && d.getDayOfMonth() == today.getDayOfMonth()
                            && d.getYear() < today.getYear();
                })
                .toList();
        if (matches.isEmpty()) {
            return null;
        }
        int yearsAgo = matches.stream()
                .mapToInt(p -> today.getYear() - p.getTakenAt().atZone(AppTime.ZONE).toLocalDate().getYear())
                .min().orElse(1);
        String title = yearsAgo == 1 ? "去年今天" : yearsAgo + " 年前的今天";
        String location = matches.stream().map(Photo::getLocationName)
                .filter(l -> l != null && !l.isBlank()).findFirst().orElse(null);
        String description = location != null
                ? "你們在「" + location + "」留下了 " + matches.size() + " 張回憶"
                : "你們留下了 " + matches.size() + " 張回憶";
        return new HomeMemory(title, description, matches.size());
    }

    private Photo load(String id) {
        Photo p = photoRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Photo not found"));
        if (!p.getCoupleId().equals(coupleContext.requireCoupleId())) {
            throw ApiException.notFound("Photo not found");
        }
        return p;
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return tags.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining(","));
    }
}
