package com.io.gui;

public class IOPatcher {

    private class IOPatch {

        public IOPatch next, prev;
        public String oldFragment, newFragment;
        public int position, timestamp;

        public IOPatch(String oldFragment, String newFragment, int position, int timestamp) throws Exception {
            if (position < 0) {
                throw new Exception("Patch position cannot be negative");
            }

            this.oldFragment = oldFragment;
            this.newFragment = newFragment;
            this.position = position;
            this.timestamp = timestamp;
        }

        public String applyTo(String file) throws Exception {
            if (position > file.length()) {
                throw new Exception("This patch goes outside the given file");
            }

            String first = file.substring(0, position);
            String last = file.substring(position + oldFragment.length());

            //System.out.println("[" + first + "][" + newFragment + "][" + last + "]");
            return first + newFragment + last;
        }
    }

    private class IOPatchList {

        private IOPatch head;
        private IOPatch tail;

        public IOPatchList() {
            head = null;
            tail = null;
        }

        public void addPatch(IOPatch patch) {
            if (head == null) {
                head = tail = patch;
                patch.next = patch.prev = null;
            }
            else {
                IOPatch beforePatch = tail;
                while (beforePatch != null && patch.timestamp < beforePatch.timestamp) {
                    beforePatch = beforePatch.prev;
                }

                if (beforePatch == null) {
                    //Insert before head
                    head.prev = patch;
                    patch.next = head;
                    patch.prev = null;
                    head = patch;
                }
                else {
                    //Insert between beforePatch and afterPatch
                    IOPatch afterPatch = beforePatch.next;
                    beforePatch.next = patch;
                    patch.prev = beforePatch;

                    if (afterPatch == null) {
                        tail = patch;
                        patch.next = null;
                    }
                    else {
                        afterPatch.prev = patch;
                        patch.next = afterPatch;
                    }
                }
            }
        }
    }

    public int history = 20;

    private String baseFile;
    private IOPatchList patches;

    public IOPatcher(String baseFile) {
        this.baseFile = baseFile;
        patches = new IOPatchList();
    }

    public void addPatch(String oldFragment, String newFragment, int position, int timestamp) {
        try {
            IOPatch patch = new IOPatch(oldFragment, newFragment, position, timestamp);
            patches.addPatch(patch);
        }
        catch (Exception ex) {
            System.out.println("Invalid patch did not get added to the list");
        }
    }

    public void rebaseFile() {

        String newBase;

        if (this.history == 0 && this.patches.tail != null) {
            newBase = buildFile(this.baseFile, this.patches.head, null);
            this.patches.head = null;
            this.patches.tail = null;
        }
        else {
            IOPatch stopPatch = this.patches.tail;
            for (int i = 1; i < this.history; i++) {
                if (stopPatch == null) {
                    break;
                }

                stopPatch = stopPatch.prev;
            }

            if (stopPatch == null) {
                System.out.println("No rebase needed");
                return;
            }

            newBase = buildFile(this.baseFile, this.patches.head, stopPatch);

            this.patches.head = stopPatch;
            stopPatch.prev = null;
        }

        //System.out.println("Old Base:\n" + this.baseFile + "\n\nNew Base:\n" + newBase + "\n");
        this.baseFile = newBase;
    }

    public String buildFile() {
        return buildFile(this.baseFile, this.patches.head, null);
    }

    private String buildFile(String file, IOPatch patch, IOPatch stopPatch) {

        if (patch == stopPatch) {
            return file;
        }

        try {
            String newFile = patch.applyTo(file);
            IOPatch nextPatch = patch.next;
            return buildFile(newFile, nextPatch, stopPatch);
        }
        catch (Exception ex) {
            System.out.println("Failed to apply all patches");
            return file;
        }
    }

    public int getLastChangePosition() {
        IOPatch lastPatch = this.patches.tail;
        return lastPatch.position - lastPatch.oldFragment.length() + lastPatch.newFragment.length();
    }

    public void printPatchList() {

        String sep = "-------------------\n";
        String output = sep;
        output += this.baseFile + "\n";
        output += sep;

        IOPatch patch = this.patches.head;

        while (patch != null) {
            output += patch.timestamp + ": [" + patch.oldFragment + "] -> [" + patch.newFragment + "] at " + patch.position + "\n";
            patch = patch.next;
        }

        output += sep;
        output += sep;

        System.out.println(output);
    }


}
