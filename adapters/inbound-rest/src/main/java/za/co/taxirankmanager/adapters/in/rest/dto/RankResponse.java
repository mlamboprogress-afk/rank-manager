package za.co.taxirankmanager.adapters.in.rest.dto;

import za.co.taxirankmanager.rank.domain.Rank;

public record RankResponse(String id, String name, String locationDescription, boolean active) {

    public static RankResponse from(Rank rank) {
        return new RankResponse(
                rank.getId().value().toString(),
                rank.getName(),
                rank.getLocationDescription(),
                rank.isActive());
    }
}
