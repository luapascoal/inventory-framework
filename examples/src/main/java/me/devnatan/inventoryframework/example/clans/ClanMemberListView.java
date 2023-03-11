package me.devnatan.inventoryframework.example.clans;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.OpenContext;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.state.State;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public final class ClanMemberListView extends View {

    private final State<List<ClanMember>> membersListState;
    private final State<Clan> clanState;
    private State<Pagination> pagination;

    public ClanMemberListView(@NotNull ClansManager clansManager) {
        this.clanState = state(ctx -> {
            // TODO fix this example using ctx.get(...)
            return clansManager.getClan("");
        });
        this.membersListState = state(ctx -> {
            // TODO fix this example using ctx.get(...)
            return clansManager.getMembers(UUID.randomUUID());
        });
        //        this.pagination = pagination(membersListState, this::createPaginationItem);
    }

    @Override
    public void onInit(ViewConfigBuilder config) {
        config.size(6).layout("        ", " OOOOOOO ", " OOOOOOO ", " OOOOOOO ", " OOOOOOO ", "  <   >  ");
    }

    @Override
    public void onOpen(OpenContext ctx) {
        final Clan clan = clanState.get(ctx);
        final List<ClanMember> memberList = membersListState.get(ctx);
        ctx.setTitle(String.format("[%s] Members (%d)", clan.getTag(), memberList.size()));
    }

    @Override
    public void onFirstRender(RenderContext ctx) {
        final Pagination localPagination = pagination.get(ctx);
        ctx.layoutSlot('<').onClick($ -> localPagination.back());
        ctx.layoutSlot('>').onClick($ -> localPagination.advance());
    }

    private ItemStack createPaginationItem(ClanMember member) {
        final ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) requireNonNull(stack.getItemMeta());

        meta.setDisplayName(member.getName());
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(member.getUUID()));
        stack.setItemMeta(meta);

        return stack;
    }
}
