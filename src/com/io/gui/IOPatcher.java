package com.io.gui;

import java.util.LinkedList;

public class IOPatcher extends diff_match_patch {

    public Patch makePatch(String originalText, String oldFragment, String newFragment, int position) {

        Patch patch = new Patch();

        int lastIndex = originalText.length();
        int start = Math.max(position - this.Patch_Margin, 0);
        int end = Math.min(lastIndex, position + oldFragment.length() + this.Patch_Margin);

        patch.start1 = patch.start2 = start;
        patch.length1 = end - start;

        patch.length2 = patch.length1 - oldFragment.length() + newFragment.length();

        //Match a little before changes
        patch.diffs.add(new Diff(Operation.EQUAL, originalText.substring(start, position)));

        //Add delete if there is something to delete/replace
        if (oldFragment.length() > 0) {
            patch.diffs.add(new Diff(Operation.DELETE, oldFragment));
        }

        //Add insert if there is something to insert
        if (newFragment.length() > 0) {
            patch.diffs.add(new Diff(Operation.INSERT, newFragment));
        }

        //Match a little after changes
        patch.diffs.add(new Diff(Operation.EQUAL, originalText.substring(position + oldFragment.length(), end)));

        return patch;
    }

    public LinkedList<Patch> makePatchAsList(String originalText, String oldFragment, String newFragment, int position) {

        LinkedList<Patch> patches = new LinkedList<Patch>();
        Patch patch = this.makePatch(originalText, oldFragment, newFragment, position);
        patches.add(patch);

        return patches;
    }

}
