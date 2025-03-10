package me.wesley1808.servercore.mixin.optimizations.players;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

/**
 * Based on: Paper (Optimize-the-advancement-data-player-iteration-to-be-O(N)-rather-than-O(N^2).patch)
 * <p>
 * Patch Author: Wyatt Childers (wchilders@nearce.com)
 * <br>
 * License: GPL-3.0 (licenses/GPL.md)
 */
@Mixin(value = PlayerAdvancements.class, priority = 900)
public abstract class PlayerAdvancementsMixin {
    private static final int PARENT_OF_ITERATOR = 2;
    private static final int ITERATOR = 1;
    private static final int ROOT = 0;

    @Shadow
    @Final
    private Set<Advancement> visible;

    @Shadow
    @Final
    private Set<Advancement> visibilityChanged;

    @Shadow
    @Final
    private Map<Advancement, AdvancementProgress> advancements;

    @Shadow
    @Final
    private Set<Advancement> progressChanged;

    @Shadow
    protected abstract boolean shouldBeVisible(Advancement advancement);

    /**
     * @author Wyatt Childers
     * @reason Optimize the advancement data player iteration to be O(N) rather than O(N^2)
     */
    @Overwrite
    private void ensureVisibility(Advancement advancement) {
        this.fastEnsureVisibility(advancement, ROOT);
    }

    private void fastEnsureVisibility(Advancement advancement, int entryPoint) {
        boolean shouldBeVisible = this.shouldBeVisible(advancement);
        boolean isVisible = this.visible.contains(advancement);
        if (shouldBeVisible && !isVisible) {
            this.visible.add(advancement);
            this.visibilityChanged.add(advancement);
            if (this.advancements.containsKey(advancement)) {
                this.progressChanged.add(advancement);
            }
        } else if (!shouldBeVisible && isVisible) {
            this.visible.remove(advancement);
            this.visibilityChanged.add(advancement);
        }

        if (shouldBeVisible != isVisible && advancement.getParent() != null) {
            // If we're not coming from an iterator consider this to be a root entry, otherwise
            // market that we're entering from the parent of an iterator.
            this.fastEnsureVisibility(advancement.getParent(), entryPoint == ITERATOR ? PARENT_OF_ITERATOR : ROOT);
        }

        // If this is true, we've gone through a child iteration, entered the parent, processed the parent
        // and are about to reprocess the children. Stop processing here to prevent O(N^2) processing.
        if (entryPoint == PARENT_OF_ITERATOR) {
            return;
        }

        for (Advancement child : advancement.getChildren()) {
            this.fastEnsureVisibility(child, ITERATOR);
        }
    }
}