package com.guflimc.treasurechests.spigot.data.converters;

import com.guflimc.treasurechests.spigot.data.beans.ParticleEffect;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.regex.Pattern;

@Converter(autoApply = true)
public class ParticleEffectConverter implements AttributeConverter<ParticleEffect, String> {

    @Override
    public String convertToDatabaseColumn(ParticleEffect attribute) {
        return attribute.type().name() + ";" + attribute.pattern().name();
    }

    @Override
    public ParticleEffect convertToEntityAttribute(String dbData) {
        String[] split = dbData.split(Pattern.quote(";"));
        return new ParticleEffect(
                ParticleEffect.ParticleType.valueOf(split[0]),
                ParticleEffect.ParticlePattern.valueOf(split[1])
        );
    }
}