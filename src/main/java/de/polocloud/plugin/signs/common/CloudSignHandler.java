package de.polocloud.plugin.signs.common;

import de.polocloud.api.CloudAPI;
import de.polocloud.api.service.CloudService;
import de.polocloud.api.service.ServiceState;
import de.polocloud.plugin.signs.common.events.CollectiveCloudEvents;
import de.polocloud.plugin.signs.config.SignConfig;
import de.polocloud.plugin.signs.config.gson.Document;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class CloudSignHandler {

    @Getter private static CloudSignHandler instance;

    private final List<CloudSign> cloudSigns = new ArrayList<>();
    private final SignConfig config;

    public CloudSignHandler() {
        instance = this;

        File file = new File("plugins/cloudsigns/", "config.json");
        if(file.exists()) {
            this.config = new Document(file).get(SignConfig.class);
        }else{
            new File("plugins/cloudsigns/").mkdir();
            new Document(this.config = new SignConfig()).write(file);
        }

        config.getCloudsigns().forEach(it -> addCloudSign(it));

        new CollectiveCloudEvents();
        confirmSigns();
    }

    public void confirmSigns() {
        getPossibleServices().forEach(it -> getPossibleSignByGroup(it.getGroup().getName()).ifPresent(cloudSign -> cloudSign.create(it)));
    }

    public Optional<CloudSign> getCloudSign(Location location) {
        return cloudSigns.stream().filter(it -> it.getLocation().equals(location)).findAny();
    }

    private List<CloudService> getPossibleServices() {
        return CloudAPI.getInstance().getServiceManager().getAllServicesByState(ServiceState.ONLINE).stream().filter(it -> !hasCloudSign(it)).toList();
    }

    public void addCloudSign(@NotNull Location location, @NotNull String group) {
        cloudSigns.add(new CloudSign(location, group));
    }

    public void addCloudSign(@NotNull CloudSignInfo info) {
        cloudSigns.add(new CloudSign(info.getLocation(), info.getPossibleGroup()));
    }

    public Optional<CloudSign> getPossibleSignByGroup(String groupName) {
        return cloudSigns.stream().filter(it -> it.getPossibleGroup().equalsIgnoreCase(groupName) && it.getService() == null).findAny();
    }

    public Optional<CloudSign> getSignByService(String service) {
        return cloudSigns.stream().filter(it -> it.getService() != null && it.getService().equalsIgnoreCase(service)).findAny();
    }

    public boolean hasCloudSign(CloudService cloudService) {
        return cloudSigns.stream().anyMatch(it -> it.getService() != null && it.getService().equalsIgnoreCase(cloudService.getName()));
    }

    public void updateAllSigns(){
        cloudSigns.forEach(it -> it.update());
    }

}
