package com.shopbee.imageservice.image;

import com.shopbee.imageservice.page.PageRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImageRepository implements PanacheRepository<Image> {

    public Optional<Image> findAvatarByUserId(Long userId) {
        return find("avatar = ?1 AND userId = ?2", true, userId).firstResultOptional();
    }

    public List<Image> find(Long userId, PageRequest pageRequest) {
        return find("userId", userId).page(pageRequest.getPage() - 1, pageRequest.getSize()).list();
    }

    public long count(Long userId) {
        return count("userId", userId);
    }

}
