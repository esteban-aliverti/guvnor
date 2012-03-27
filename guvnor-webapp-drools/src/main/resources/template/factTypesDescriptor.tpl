<factTypes>
$concepts.keys:{concept|
    <factType class="$concept$">
        <fields>
        $concepts.(concept).keys:{property|
            <field name="$property$" type="$concepts.(concept).(property)$"/>
        }$
        </fields>
    </factType>
}$
</factTypes>