package com.example.limit.semaphoreblocker.model.dto;

import com.example.limit.semaphoreblocker.model.property.SemaphoreProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.Semaphore;



@Data
@Accessors(chain = true)
@NoArgsConstructor
public class SemaphoreData {

    private SemaphoreProperty.SemaphoreLimit limit;

    private Semaphore semaphore;
}
