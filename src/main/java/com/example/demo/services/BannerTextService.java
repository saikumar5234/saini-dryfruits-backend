package com.example.demo.services;

import com.example.demo.model.BannerText;
import com.example.demo.Repository.BannerTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    
    public void createOrUpdateBannerText(String textJson, Boolean isActive) {
        List<BannerText> existingBanners = bannerTextRepository.findAll();
        BannerText bannerText;
        
        if (existingBanners.isEmpty()) {
            // Create new banner
            bannerText = new BannerText();
        } else {
            // Update existing banner (assuming single banner)
            bannerText = existingBanners.get(0);
        }
        
        bannerText.setTextJson(textJson);
        if (isActive != null) {
            bannerText.setIsActive(isActive);
        }
        
        bannerTextRepository.save(bannerText);
    }
}