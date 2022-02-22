package com.dumbapp.memorydance;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component
public interface MemoryDanceApplicationComponent {
    void inject(final MainActivity activity);

    void inject(final CameraFragment fragment);
}
