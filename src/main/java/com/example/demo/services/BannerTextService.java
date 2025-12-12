package com.example.demo.services;

import com.example.demo.model.BannerText;
import com.example.demo.Repository.BannerTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class BannerTextService {
    
    @Autowired
    private BannerTextRepository bannerTextRepository;
    
    private static final String DEFAULT_TEXT = "Welcome to Saini Mewa Stores - Your trusted source for premium dry fruits! 🥜✨";
    
    public String getActiveBannerText() {
        Optional<BannerText> activeBanner = bannerTextRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc();
        
        if (activeBanner.isPresent()) {
            return activeBanner.get().getText();
        }
        
        return DEFAULT_TEXT;
    }
    
    public BannerText createOrUpdateBannerText(String text) {
        // Deactivate all existing banners
        bannerTextRepository.findAll().forEach(banner -> {
            banner.setIsActive(false);
            bannerTextRepository.save(banner);
        });
        
        // Create new active banner
        BannerText newBanner = new BannerText();
        newBanner.setText(text);
        newBanner.setIsActive(true);
        
        return bannerTextRepository.save(newBanner);
    }
}