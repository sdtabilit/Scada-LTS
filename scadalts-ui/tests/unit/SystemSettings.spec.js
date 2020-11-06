import { createLocalVue, mount, shallowMount } from '@vue/test-utils'
import SystemSettings from '@/views/SystemSettings.vue'
import systemSettings from '../mocks/store/systemSettings'
import { assert, expect } from "chai";
import Vuex from 'vuex';
import { cloneDeep } from 'lodash';
import store from '../../src/store';

const localVue = createLocalVue()
localVue.use(Vuex)


const i18n = {
    t: () => { },
};

describe("SystemSettings.vue", () => {
    let store = new Vuex.Store({
        modules: {
            systemSettings: cloneDeep(systemSettings)
        },
        state: { },
        mutations: { },
        actions: { }
    })

    it('test', () => {
        const wrapper = shallowMount(SystemSettings, {
            store, localVue, mocks: {
                i18n,
                $t: () => { },
            }
        })
        expect(wrapper.exists()).to.equal(true)
        expect(wrapper.find('h1').exists()).to.equal(true)
        // expect(wrapper.findAll('button').exists()).to.equal(true)
    })
})