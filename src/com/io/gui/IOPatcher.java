package com.io.gui;

import java.util.LinkedList;

public class IOPatcher extends diff_match_patch {

    public Patch makeInsertPatch(String originalText, String newFragment, int newFragmentPosition) {

        Patch patch = new Patch();

        int lastIndex = originalText.length();
        int start = Math.max(newFragmentPosition - (this.Patch_Margin * 2), 0);
        int end = Math.min(lastIndex, newFragmentPosition + (this.Patch_Margin * 2));

        patch.start1 = patch.start2 = start;

        patch.length1 = end - start;
        patch.length2 = patch.length1 + newFragment.length();

        patch.diffs.add(new Diff(Operation.EQUAL, originalText.substring(start, newFragmentPosition)));
        patch.diffs.add(new Diff(Operation.INSERT, newFragment));
        patch.diffs.add(new Diff(Operation.EQUAL, originalText.substring(newFragmentPosition, end)));

        return patch;
    }

    public LinkedList<Patch> makeInsertPatchAsList(String originalText, String newFragment, int newFragmentPosition) {

        LinkedList<Patch> patches = new LinkedList<Patch>();
        Patch patch = this.makeInsertPatch(originalText, newFragment, newFragmentPosition);
        patches.add(patch);

        return patches;
    }

}
