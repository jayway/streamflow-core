Restore data
============

#. Firstly you needed files created at :doc:`backup_data`

#. Then copy files obtained from backup to same folder **{StreamflowServer}/data/backup**.

#. Open VisualVM and click MBeans tab. There open path **Qi4j/StreamflowServer/Manager**.

#. Then click at *Operations* tab. There should **restore** button. Click on it.

.. image:: images/restore.png
    :align: center
    :width: 100%

.. hint::
   Or you can just fully replace **{StreamflowServer}** folder and reload server

.. note::
    For more detailed information about files stored at system you can check :ref:`local-files-label-reference`