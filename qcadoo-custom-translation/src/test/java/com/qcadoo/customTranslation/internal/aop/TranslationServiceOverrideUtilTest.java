package com.qcadoo.customTranslation.internal.aop;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.customTranslation.api.CustomTranslationResolver;
import com.qcadoo.customTranslation.constants.CustomTranslationContants;
import com.qcadoo.plugin.api.PluginStateResolver;

public class TranslationServiceOverrideUtilTest {

    private TranslationServiceOverrideUtil translationServiceOverrideUtil;

    @Mock
    private PluginStateResolver pluginStateResolver;

    @Mock
    private CustomTranslationResolver customTranslationResolver;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        translationServiceOverrideUtil = new TranslationServiceOverrideUtil();

        ReflectionTestUtils.setField(translationServiceOverrideUtil, "pluginStateResolver", pluginStateResolver);
        ReflectionTestUtils.setField(translationServiceOverrideUtil, "customTranslationResolver", customTranslationResolver);
    }

    @Test
    public void shouldReturnTrueWhenShouldOverrideTranslation() {
        // given
        String key = "key";
        Locale locale = new Locale("pl");

        ReflectionTestUtils.setField(translationServiceOverrideUtil, "useCustomTranslations", true);

        given(pluginStateResolver.isEnabled(CustomTranslationContants.PLUGIN_IDENTIFIER)).willReturn(true);
        given(customTranslationResolver.isCustomTranslationActive(key, locale)).willReturn(true);

        // when
        boolean result = translationServiceOverrideUtil.shouldOverrideTranslation(key, locale);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenShouldOverrideTranslation() {
        // given
        String key = "key";
        Locale locale = new Locale("pl");

        ReflectionTestUtils.setField(translationServiceOverrideUtil, "useCustomTranslations", false);

        given(pluginStateResolver.isEnabled(CustomTranslationContants.PLUGIN_IDENTIFIER)).willReturn(false);
        given(customTranslationResolver.isCustomTranslationActive(key, locale)).willReturn(false);

        // when
        boolean result = translationServiceOverrideUtil.shouldOverrideTranslation(key, locale);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnNullWhenGetCustomTranslationIfCustomTranslationIsNull() {
        // given
        String key = "key";
        Locale locale = new Locale("pl");
        String[] args = null;

        given(customTranslationResolver.getCustomTranslation(key, locale, args)).willReturn(null);

        // when
        String result = translationServiceOverrideUtil.getCustomTranslation(key, locale, args);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnCustomTranslationWhenGetCustomTranslation() {
        // given
        String key = "key";
        Locale locale = new Locale("pl");
        String[] args = null;

        String translation = "translation";

        given(customTranslationResolver.getCustomTranslation(key, locale, args)).willReturn(translation);

        // when
        String result = translationServiceOverrideUtil.getCustomTranslation(key, locale, args);

        // then
        assertEquals(translation, result);
    }

}