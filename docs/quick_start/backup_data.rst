Backup data
===========

How to backup?
--------------

Backup is made with the help of `VisualVM <https://visualvm.github.io>`_.

Let's describe this step by step.

#. Open VisualVM and click MBeans tab. There open path **Qi4j/StreamflowServer/Manager**.

.. image:: https://raw.githubusercontent.com/jayway/streamflow-core/develop/docs/quick_start/images/manager_location.png
    :align: center
    :width: 100%

#. Then click at *Operations* tab. There should **backup** button. Click on it.

.. image:: https://raw.githubusercontent.com/jayway/streamflow-core/develop/docs/quick_start/images/backup.png
    :align: center
    :width: 100%

#. Where is placed backup read at `Where is data?`_


Where is data?
--------------

Backup placed at **`{StreamflowServer}/data/backup`** folder.

.. note::
    For more detailed information about files stored at system you can check :ref:`local-files-label-reference`

.. important::
    If you have necessary data at this location,
    you need it move to another folder, otherwise data will be lost.

.. important::
    To perform complete backup recommended just to copy entire data folder in order to fix make easy revert.

    For *`Windows`* is
        If launched directly: **`C:\Users\<user>\\AppData\\Roaming\\StreamflowServer\\`**

        If launched as service: **`C:\Windows\\System32\\config\\systemprofile\\Application Data\\StreamflowServer`**

    For *Linux* is **<User_home>/.StreamFlowServer**
