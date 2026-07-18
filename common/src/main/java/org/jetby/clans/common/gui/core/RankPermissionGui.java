package org.jetby.clans.common.gui.core;

import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.gui.Gui;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.service.clan.member.rank.Permission;
import org.jetby.clans.api.service.clan.member.rank.PermissionRegistry;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.TreexClans;

public class RankPermissionGui extends Gui {

    private final Rank rank;

    public RankPermissionGui(@NotNull GuiContext ctx) {
        super(ctx);
        this.rank = ctx.get(Rank.class);


        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type == null) return;
            String permName = type.replace("perm-", "").toUpperCase();
            RankPerm perm = RankPerm.valueOf(permName);

            if (!getClan().getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.SETRANK)) return;
            if (getClan().getMember(player.getUniqueId()).getRank().equals(rank)) return;
            if (!getClan().getLeader().getRank().perms().contains(perm)) return;
            if (!getClan().getMember(player.getUniqueId()).getRank().perms().contains(perm)) return;

            if (rank.perms().contains(perm)) {
                rank.perms().remove(perm);
            } else {
                rank.perms().add(perm);
            }
            TreexClans.getInstance().getStorage().saveClan(getClan());
            refresh();
        });
    }

    @Override
    public void refresh() {


        for (Permission perm : PermissionRegistry.getAll()) {

            setReplace("{" + perm.getId().toLowerCase() + "_status}", getStatus(rank.perms().contains(perm)));
            setReplace("{" + perm.getId().toLowerCase() + "_status_boolean}", String.valueOf(rank.perms().contains(perm)));

            setReplace("%" + perm.getId().toLowerCase() + "_status%", getStatus(rank.perms().contains(perm)));
            setReplace("%" + perm.getId().toLowerCase() + "_status_boolean%", String.valueOf(rank.perms().contains(perm)));
        }

        setReplace("{rank}", rank.name());
        setReplace("%rank%", rank.name());

        super.refresh();

    }

    private static String getStatus(boolean status) {
        return TreexClans.getInstance().getMessages().getCleanMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }

}
