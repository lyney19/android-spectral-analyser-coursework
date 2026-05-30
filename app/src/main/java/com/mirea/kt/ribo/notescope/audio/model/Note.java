package com.mirea.kt.ribo.notescope.audio.model;

public record Note(String name, int octave) {
    private static final String[] NOTES = {
            "C", "C#", "D", "D#", "E", "F",
            "F#", "G", "G#", "A", "A#", "B"
    };

    public static Note fromMidi(int midiNote) {
        int noteIndex = Math.floorMod(midiNote, 12);
        int octave = (midiNote / 12) - 1;

        return new Note(NOTES[noteIndex], octave);
    }

    @Override
    public String toString() {
        return name + octave;
    }
}