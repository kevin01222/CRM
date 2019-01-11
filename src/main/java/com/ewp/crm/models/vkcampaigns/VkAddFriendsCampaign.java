package com.ewp.crm.models.vkcampaigns;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vk_add_friends_campaign")
public class VkAddFriendsCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "campaign_name", unique = true)
    private String campaignName;

    @Column(name = "app_id")
    private Long appId;

    @Column(name = "vk_user_id")
    private Long vkUserId;

    @Column(name = "vk_user_token")
    private String vkUserToken;

    @Column(name = "request_text")
    private String requestText;

    @JoinTable(name = "vk_add_friends_campaign_vk_user",
            joinColumns = {@JoinColumn(name = "campaign_id")},
            inverseJoinColumns = {@JoinColumn(name = "vk_id")})
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST,
            CascadeType.MERGE})
    private Set<VkUser> vkUsersToAdd = new HashSet<>();

    public VkAddFriendsCampaign() {
    }

    public VkAddFriendsCampaign(String campaignName, Long appId, Long vkUserId, String vkUserToken,
                                String requestText, Set<VkUser> vkUsersToAdd) {
        this.campaignName = campaignName;
        this.appId = appId;
        this.vkUserId = vkUserId;
        this.vkUserToken = vkUserToken;
        this.requestText = requestText;
        this.vkUsersToAdd = vkUsersToAdd;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getVkUserId() {
        return vkUserId;
    }

    public void setVkUserId(Long vkUserId) {
        this.vkUserId = vkUserId;
    }

    public String getVkUserToken() {
        return vkUserToken;
    }

    public void setVkUserToken(String vkUserToken) {
        this.vkUserToken = vkUserToken;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public Set<VkUser> getVkUsersToAdd() {
        return vkUsersToAdd;
    }

    public void setVkUsersToAdd(Set<VkUser> vkUsersToAdd) {
        this.vkUsersToAdd = vkUsersToAdd;
    }
}
