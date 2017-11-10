/*
Copyright 2016 shizhefei（LuckyJayce）https://github.com/LuckyJayce
Copyright 2016 drakeet.   https://github.com/drakeet

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.shizhefei.view.multitype;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;

import com.shizhefei.view.multitype.data.IParcelableData;
import com.shizhefei.view.multitype.provider.FragmentData;
import com.shizhefei.view.multitype.provider.FragmentDataProvider;
import com.shizhefei.view.multitype.provider.ViewProvider;

import java.util.HashMap;
import java.util.Map;



public class ItemBinderFactory {

    private static final ItemBinderFactory INSTANCE = new ItemBinderFactory(1000);

    static {
        registerStaticProvider(View.class, new ViewProvider());
    }

    private FragmentDataProvider fragmentDataProvider;

    private FragmentManager fragmentManager;

    private Map<Class<?>, ItemViewProviderSet> providerSets = new HashMap<>();
    private SparseArray<ItemViewProvider> providerIndex = new SparseArray<>();
    private int providerType = 0;

    private ItemBinderFactory(int providerTypeStart) {
        providerType = providerTypeStart;
    }

    public ItemBinderFactory() {

    }


    public ItemBinderFactory(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        registerProvider(FragmentData.class, fragmentDataProvider = new FragmentDataProvider(fragmentManager));
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    FragmentDataProvider getFragmentDataProvider() {
        return fragmentDataProvider;
    }


    public ItemViewProvider getProvider(int providerType) {
        ItemViewProvider provider = providerIndex.get(providerType);
        if (provider == null) {//如果没有，则通过静态实例去根据type去获取
            if (this != INSTANCE) {
                provider = INSTANCE.getProvider(providerType);
            }
        }
        return provider;
    }


    public ItemViewProvider getProvider(Object data) {
        PT vt = getPT(data);
        return vt == null ? null : getPT(data).provider;
    }


    public boolean hasProvider(Object data) {
        PT vt = getPT(data);
        return vt != null;
    }

    private Class<?> getDataClass(Object data) {
        if (data instanceof IParcelableData) {
            IParcelableData parcelableImp = (IParcelableData) data;
            return parcelableImp.getData().getClass();
        }
        return data.getClass();
    }


    private PT getPT(Object data) {
        Class<?> dataClass = getDataClass(data);
        ItemViewProviderSet itemProviderSet = providerSets.get(dataClass);

        if (itemProviderSet != null) {

            int index = itemProviderSet.select(data);
            int providerType = itemProviderSet.getProviderType(index);
            ItemViewProvider provider = itemProviderSet.getItemProvider(index);
            return vtInstance.set(provider, providerType);

        } else {

            for (Map.Entry<Class<?>, ItemViewProviderSet> entry : providerSets.entrySet()) {
                Class<?> registerDataClass = entry.getKey();

                if (registerDataClass.isAssignableFrom(dataClass)) {
                    itemProviderSet = entry.getValue();
                    int index = itemProviderSet.select(data);
                    int providerType = itemProviderSet.getProviderType(index);
                    ItemViewProvider provider = itemProviderSet.getItemProvider(index);
                    return vtInstance.set(provider, providerType);
                }
            }
        }
        if (this != INSTANCE) {
            return INSTANCE.getPT(data);
        }
        return null;
    }


    public <DATA> void registerProvider(Class<DATA> dataClass, ItemViewProvider<DATA> provider) {
        registerProvider(dataClass, new ItemViewProviderSet<DATA>(provider) {
            @Override
            protected int selectIndex(DATA data) {
                return 0;
            }
        });
    }


    public <DATA> void registerProvider(Class<DATA> dataClass, ItemViewProviderSet<DATA> itemProviderSet) {
        providerSets.put(dataClass, itemProviderSet);
        itemProviderSet.setProviderType(providerType);
        for (int i = 0; i < itemProviderSet.size(); i++) {
            providerIndex.put(providerType, itemProviderSet.getItemProvider(i));
            providerType++;
        }
    }


    public static <DATA> void registerStaticProvider(Class<DATA> dataClass, ItemViewProvider<DATA> provider) {
        INSTANCE.registerProvider(dataClass, provider);
    }


    <DATA> ItemBinder<DATA> buildItemData(DATA data) {
        PT pt = getPT(data);
        if (pt == null) {
            throw new RuntimeException("没有注册" + getDataClass(data) + " 对应的ItemProvider");
        }
        int type = pt.providerType;
        //每个Fragment都是唯一的type
        if (pt.provider.isUniqueProviderType(data)) {
            type = providerType;
            providerType++;
        }
        return new ItemBinder(data, pt.provider, type);
    }

//    public <DATA> ItemBinder<DATA> buildItemData(SerializableData data, ItemViewProvider<DATA> provider) {
//        return buildItemData((DATA) data.getData(), provider);
//    }
//
//    /**
//     * 创建ItemData，ItemData用于adapter构造显示的界面
//     * 里面包含data，provider，providerType
//     *
//     * @param data     数据
//     * @param provider provider
//     * @param <DATA>   数据泛型
//     * @return ItemBinder
//     */
//    public <DATA> ItemBinder<DATA> buildItemData(DATA data, ItemViewProvider<DATA> provider) {
//        int type;
//        //每个Fragment都是唯一的type
//        if (provider.isUniqueProviderType(data)) {
//            type = providerType;
//            providerType++;
//        } else {
//            int index = providerIndex.indexOfValue(provider);
//            if (index < 0) {
//                if (this != INSTANCE && (INSTANCE.hasProvider(provider))) {
//                    type = INSTANCE.getProviderType(provider);
//                } else {
//                    type = providerType;
//                    providerType++;
//                }
//            } else {
//                type = providerIndex.keyAt(index);
//            }
//        }
//        return new ItemBinder<>(data, provider, type);
//    }

    private boolean hasProvider(ItemViewProvider provider) {
        return providerIndex.indexOfValue(provider) > 0;
    }

    private int getProviderType(ItemViewProvider provider) {
        int index = providerIndex.indexOfValue(provider);
        return providerIndex.keyAt(index);
    }

    private PT vtInstance = new PT();

    private static class PT {
        ItemViewProvider provider;
        int providerType;

        public PT() {
        }

        public PT set(ItemViewProvider provider, int providerType) {
            this.provider = provider;
            this.providerType = providerType;
            return this;
        }
    }
}
