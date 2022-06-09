package at.emielregis.backend.data.entities;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
public class SteamAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 17)
    private String id64;

    @OneToOne(fetch = FetchType.EAGER)
    private CSGOInventory csgoInventory;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> friendIds;

    public SteamAccount() {

    }

    public String toString() {
        return "SteamAccount{" +
            "id=" + id +
            ", id64='" + id64 + '\'' +
            ", csgoInventory=" + csgoInventory +
            ", friendIds=" + friendIds +
            '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getId64() {
        return id64;
    }

    public void setId64(String id64) {
        this.id64 = id64;
    }

    public CSGOInventory getCsgoInventory() {
        return csgoInventory;
    }

    public void setCsgoInventory(CSGOInventory csgoInventory) {
        this.csgoInventory = csgoInventory;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    public static class SteamAccountBuilder {
        private String id64;
        private CSGOInventory csgoInventory;
        private List<String> friendIds;

        private SteamAccountBuilder() {

        }

        public static SteamAccountBuilder create() {
            return new SteamAccountBuilder();
        }

        public SteamAccountBuilder withId64(String id64) {
            this.id64 = id64;
            return this;
        }

        public SteamAccountBuilder withCSGOInventory(CSGOInventory csgoInventory) {
            this.csgoInventory = csgoInventory;
            return this;
        }

        public SteamAccountBuilder withFriendIds(List<String> friendIds) {
            this.friendIds = friendIds;
            return this;
        }

        public SteamAccount build() {
            SteamAccount account = new SteamAccount();
            account.setId64(id64);
            account.setCsgoInventory(csgoInventory);
            account.setFriendIds(friendIds);
            return account;
        }
    }
}
